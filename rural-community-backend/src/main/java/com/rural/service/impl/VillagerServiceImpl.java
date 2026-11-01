package com.rural.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rural.common.PageResult;
import com.rural.common.ResultCode;
import com.rural.common.exception.BusinessException;
import com.rural.dto.ImportResult;
import com.rural.dto.VillagerImportDTO;
import com.rural.dto.VillagerRequest;
import com.rural.entity.Household;
import com.rural.entity.Villager;
import com.rural.mapper.HouseholdMapper;
import com.rural.mapper.VillagerMapper;
import com.rural.service.VillagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VillagerServiceImpl implements VillagerService {

    private final VillagerMapper villagerMapper;
    private final HouseholdMapper householdMapper;

    @Override
    public PageResult<Villager> list(int pageNum, int pageSize, String name, String idCard, String specialTag) {
        LambdaQueryWrapper<Villager> wrapper = new LambdaQueryWrapper<>();
        wrapper.apply("deleted = 0")
                .like(StringUtils.hasText(name), Villager::getName, name)
                .like(StringUtils.hasText(idCard), Villager::getIdCard, idCard)
                .like(StringUtils.hasText(specialTag), Villager::getSpecialTag, specialTag)
                .orderByDesc(Villager::getId);
        Page<Villager> page = new Page<>(pageNum, pageSize);
        IPage<Villager> result = villagerMapper.selectPageByScope(page, wrapper);
        return PageResult.of(result);
    }

    @Override
    @Transactional
    public Villager create(VillagerRequest request) {
        Long count = villagerMapper.selectCount(
                new LambdaQueryWrapper<Villager>().eq(Villager::getIdCard, request.getIdCard()));
        if (count > 0) {
            throw new BusinessException("该身份证号已存在，请勿重复录入");
        }
        Villager villager = new Villager();
        BeanUtils.copyProperties(request, villager);
        villagerMapper.insert(villager);
        return villager;
    }

    @Override
    @Transactional
    public Villager update(Long id, VillagerRequest request) {
        Villager villager = villagerMapper.selectById(id);
        if (villager == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "村民不存在");
        }
        Long count = villagerMapper.selectCount(
                new LambdaQueryWrapper<Villager>()
                        .eq(Villager::getIdCard, request.getIdCard())
                        .ne(Villager::getId, id));
        if (count > 0) {
            throw new BusinessException("该身份证号已被其他村民使用");
        }
        BeanUtils.copyProperties(request, villager);
        villager.setId(id);
        villagerMapper.updateById(villager);
        return villager;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Villager villager = villagerMapper.selectById(id);
        if (villager == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "村民不存在");
        }
        villagerMapper.deleteById(id);
    }

    @Override
    @Transactional
    public ImportResult importVillagers(MultipartFile file) {
        ImportResult result = new ImportResult();
        List<VillagerImportDTO> list;
        try {
            list = EasyExcel.read(file.getInputStream())
                    .head(VillagerImportDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (Exception e) {
            throw new BusinessException("Excel 解析失败：" + e.getMessage());
        }

        int rowNum = 1;
        for (VillagerImportDTO dto : list) {
            rowNum++;
            try {
                if (!StringUtils.hasText(dto.getHouseholdNo())) {
                    result.getErrors().add("第 " + rowNum + " 行：户号为空");
                    result.setFail(result.getFail() + 1);
                    continue;
                }
                if (!StringUtils.hasText(dto.getName())) {
                    result.getErrors().add("第 " + rowNum + " 行：姓名为空");
                    result.setFail(result.getFail() + 1);
                    continue;
                }
                if (!StringUtils.hasText(dto.getIdCard())) {
                    result.getErrors().add("第 " + rowNum + " 行：身份证号为空");
                    result.setFail(result.getFail() + 1);
                    continue;
                }

                Household household = householdMapper.selectOne(
                        new LambdaQueryWrapper<Household>().eq(Household::getHouseholdNo, dto.getHouseholdNo()));
                if (household == null) {
                    // 户号不存在则自动创建户
                    household = new Household();
                    household.setHouseholdNo(dto.getHouseholdNo());
                    household.setRegisterType(1);
                    household.setMemberCount(1);
                    householdMapper.insert(household);
                }

                Long count = villagerMapper.selectCount(
                        new LambdaQueryWrapper<Villager>().eq(Villager::getIdCard, dto.getIdCard()));
                if (count > 0) {
                    result.getErrors().add("第 " + rowNum + " 行：身份证号 " + dto.getIdCard() + " 已存在");
                    result.setFail(result.getFail() + 1);
                    continue;
                }

                Villager villager = new Villager();
                villager.setHouseholdId(household.getId());
                villager.setName(dto.getName());
                villager.setIdCard(dto.getIdCard());
                villager.setGender(parseGender(dto.getGender()));
                villager.setBirthDate(parseDate(dto.getBirthDate()));
                villager.setNation(StringUtils.hasText(dto.getNation()) ? dto.getNation().trim() : "汉族");
                villager.setEducation(dto.getEducation());
                villager.setPoliticalStatus(StringUtils.hasText(dto.getPoliticalStatus()) ? dto.getPoliticalStatus().trim() : "群众");
                villager.setPhone(dto.getPhone());
                villager.setHealthStatus(StringUtils.hasText(dto.getHealthStatus()) ? dto.getHealthStatus().trim() : "健康");
                villager.setEmploymentStatus(dto.getEmploymentStatus());
                villager.setSpecialTag(dto.getSpecialTag());
                villager.setIsHouseholder(parseBool(dto.getIsHouseholder()));
                villager.setRelation(dto.getRelation());
                villager.setRemark(dto.getRemark());
                villagerMapper.insert(villager);
                result.setSuccess(result.getSuccess() + 1);
            } catch (Exception e) {
                result.getErrors().add("第 " + rowNum + " 行：" + e.getMessage());
                result.setFail(result.getFail() + 1);
            }
        }
        return result;
    }

    private Integer parseGender(String gender) {
        if ("男".equals(gender)) {
            return 1;
        }
        if ("女".equals(gender)) {
            return 2;
        }
        return 0;
    }

    private LocalDate parseDate(String dateStr) {
        if (!StringUtils.hasText(dateStr)) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseBool(String s) {
        if ("是".equals(s) || "1".equals(s) || "true".equalsIgnoreCase(s)) {
            return 1;
        }
        return 0;
    }
}
