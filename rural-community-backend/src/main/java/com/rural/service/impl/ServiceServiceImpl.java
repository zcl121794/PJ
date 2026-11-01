package com.rural.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rural.common.PageResult;
import com.rural.common.ResultCode;
import com.rural.common.exception.BusinessException;
import com.rural.dto.ServiceApplyRequest;
import com.rural.dto.ServiceHandleRequest;
import com.rural.dto.ServiceRejectRequest;
import com.rural.entity.Household;
import com.rural.entity.ServiceApply;
import com.rural.entity.ServiceItem;
import com.rural.entity.Villager;
import com.rural.mapper.HouseholdMapper;
import com.rural.mapper.ServiceApplyMapper;
import com.rural.mapper.ServiceItemMapper;
import com.rural.mapper.VillagerMapper;
import com.rural.security.LoginUser;
import com.rural.security.SecurityUtils;
import com.rural.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ServiceServiceImpl implements ServiceService {

    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ServiceItemMapper serviceItemMapper;
    private final ServiceApplyMapper serviceApplyMapper;
    private final VillagerMapper villagerMapper;
    private final HouseholdMapper householdMapper;

    @Override
    public List<ServiceItem> listItems() {
        return serviceItemMapper.selectList(null);
    }

    @Override
    public ServiceItem itemDetail(Long id) {
        ServiceItem item = serviceItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "事项不存在");
        }
        return item;
    }

    @Override
    public PageResult<ServiceApply> listApplies(int pageNum, int pageSize) {
        LambdaQueryWrapper<ServiceApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.apply("deleted = 0")
                .orderByDesc(ServiceApply::getSubmitTime);
        Page<ServiceApply> page = new Page<>(pageNum, pageSize);
        IPage<ServiceApply> result = serviceApplyMapper.selectPageByScope(page, wrapper);
        // 同一村民、同一事项存在更新的申请时，旧记录视为已重新提交
        for (ServiceApply apply : result.getRecords()) {
            Long newer = serviceApplyMapper.selectCount(new LambdaQueryWrapper<ServiceApply>()
                    .eq(ServiceApply::getVillagerId, apply.getVillagerId())
                    .eq(ServiceApply::getItemId, apply.getItemId())
                    .gt(ServiceApply::getId, apply.getId()));
            apply.setResubmitted(newer != null && newer > 0);
        }
        return PageResult.of(result);
    }

    @Override
    public ServiceApply applyDetail(Long id) {
        ServiceApply apply = serviceApplyMapper.selectById(id);
        if (apply == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "申请不存在");
        }
        return apply;
    }

    @Override
    @Transactional
    public ServiceApply apply(ServiceApplyRequest request) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser.getVillagerId() == null) {
            throw new BusinessException("当前账号未关联村民档案，无法在线申办");
        }
        ServiceItem item = serviceItemMapper.selectById(request.getItemId());
        if (item == null) {
            throw new BusinessException("事项不存在");
        }

        ServiceApply apply = new ServiceApply();
        apply.setApplyNo(generateNo("SV"));
        apply.setItemId(request.getItemId());
        apply.setVillagerId(loginUser.getVillagerId());
        apply.setStatus(0);
        apply.setApplyContent(request.getApplyContent());
        apply.setSubmitTime(LocalDateTime.now());
        apply.setDeadlineTime(LocalDateTime.now().plusDays(item.getTimeLimit() == null ? 5 : item.getTimeLimit()));
        serviceApplyMapper.insert(apply);
        return apply;
    }

    @Override
    @Transactional
    public void accept(Long id) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        ServiceApply apply = getById(id);
        if (apply.getStatus() != 0) {
            throw new BusinessException("当前状态不可受理");
        }
        apply.setStatus(1);
        apply.setHandleBy(loginUser.getUserId());
        apply.setHandleTime(LocalDateTime.now());
        serviceApplyMapper.updateById(apply);
    }

    @Override
    @Transactional
    public void gridPass(Long id, ServiceHandleRequest request) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        ServiceApply apply = getGridAuditableApply(id, loginUser);
        String comment = request != null && request.getComment() != null ? request.getComment().trim() : "";
        apply.setGridAuditBy(loginUser.getUserId());
        apply.setGridAuditTime(LocalDateTime.now());
        apply.setGridAuditComment(comment.isEmpty() ? "初审通过" : "初审通过：" + comment);
        serviceApplyMapper.updateById(apply);
    }

    @Override
    @Transactional
    public void gridReject(Long id, ServiceRejectRequest request) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        ServiceApply apply = getGridAuditableApply(id, loginUser);
        LocalDateTime now = LocalDateTime.now();
        String reason = request.getReason().trim();
        apply.setStatus(4);
        apply.setGridAuditBy(loginUser.getUserId());
        apply.setGridAuditTime(now);
        apply.setGridAuditComment("初审不通过：" + reason);
        apply.setHandleBy(loginUser.getUserId());
        apply.setHandleTime(now);
        apply.setResultContent("初审不通过：" + reason);
        serviceApplyMapper.updateById(apply);
    }

    /**
     * 网格初审条件：网格员、本网格、待审核(0)、需网格初审且尚未初审。
     */
    private ServiceApply getGridAuditableApply(Long id, LoginUser loginUser) {
        if (loginUser == null || !loginUser.getRoles().contains("GRID_WORKER")) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "仅网格员可执行初审操作");
        }
        ServiceApply apply = getById(id);
        if (apply.getStatus() == null || apply.getStatus() != 0) {
            throw new BusinessException("当前状态不可初审");
        }
        if (apply.getGridAuditBy() != null) {
            throw new BusinessException("该申请已完成初审");
        }
        ServiceItem item = serviceItemMapper.selectById(apply.getItemId());
        if (item == null) {
            throw new BusinessException("事项不存在");
        }
        if (item.getNeedGridAudit() == null || item.getNeedGridAudit() != 1) {
            throw new BusinessException("该事项无需网格初审");
        }
        Villager villager = villagerMapper.selectById(apply.getVillagerId());
        if (villager == null) {
            throw new BusinessException("申请村民信息不存在");
        }
        Household household = householdMapper.selectById(villager.getHouseholdId());
        if (loginUser.getGridId() == null || household == null
                || !Objects.equals(household.getGridId(), loginUser.getGridId())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "只能初审本网格的申请");
        }
        return apply;
    }

    @Override
    @Transactional
    public void finish(Long id, ServiceHandleRequest request) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        ServiceApply apply = getById(id);
        if (apply.getStatus() != 1) {
            throw new BusinessException("当前状态不可办结");
        }
        apply.setStatus(5);
        apply.setFinishTime(LocalDateTime.now());
        apply.setResultContent(request.getResultContent());
        serviceApplyMapper.updateById(apply);
    }

    @Override
    @Transactional
    public void approve(Long id) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        ServiceApply apply = getApprovableApply(id, loginUser);
        LocalDateTime now = LocalDateTime.now();
        apply.setStatus(5);
        apply.setHandleBy(loginUser.getUserId());
        apply.setHandleTime(now);
        apply.setFinishTime(now);
        apply.setResultContent("审核通过");
        serviceApplyMapper.updateById(apply);
    }

    @Override
    @Transactional
    public void reject(Long id, ServiceRejectRequest request) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        ServiceApply apply = getApprovableApply(id, loginUser);
        apply.setStatus(4);
        apply.setHandleBy(loginUser.getUserId());
        apply.setHandleTime(LocalDateTime.now());
        apply.setResultContent("驳回：" + request.getReason().trim());
        serviceApplyMapper.updateById(apply);
    }

    /**
     * 村委审核条件：待审核(0)，且事项无需网格初审或网格初审已通过。
     */
    private ServiceApply getApprovableApply(Long id, LoginUser loginUser) {
        if (loginUser == null || (!loginUser.isAdmin() && !loginUser.getRoles().contains("COMMITTEE"))) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "仅村委工作人员可审核办事申请");
        }
        ServiceApply apply = getById(id);
        if (apply.getStatus() == null || apply.getStatus() != 0) {
            throw new BusinessException("当前状态不可审核");
        }
        ServiceItem item = serviceItemMapper.selectById(apply.getItemId());
        if (item == null) {
            throw new BusinessException("事项不存在");
        }
        if (item.getNeedGridAudit() != null && item.getNeedGridAudit() == 1
                && apply.getGridAuditBy() == null) {
            throw new BusinessException("该事项需网格初审通过后方可审核");
        }
        return apply;
    }

    private ServiceApply getById(Long id) {
        ServiceApply apply = serviceApplyMapper.selectById(id);
        if (apply == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "申请不存在");
        }
        return apply;
    }

    private String generateNo(String prefix) {
        return prefix + LocalDateTime.now().format(NO_FORMATTER) + (int) (Math.random() * 1000);
    }
}
