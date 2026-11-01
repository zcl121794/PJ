package com.rural.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rural.common.PageResult;
import com.rural.common.ResultCode;
import com.rural.common.exception.BusinessException;
import com.rural.dto.AnnouncementRequest;
import com.rural.entity.Announcement;
import com.rural.mapper.AnnouncementMapper;
import com.rural.security.LoginUser;
import com.rural.security.SecurityUtils;
import com.rural.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementMapper announcementMapper;

    @Override
    public PageResult<Announcement> list(int pageNum, int pageSize, String keyword, String category) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(keyword), Announcement::getTitle, keyword)
                .eq(StringUtils.hasText(category), Announcement::getCategory, category);
        applyVisibility(wrapper);
        wrapper.orderByDesc(Announcement::getIsTop)
                .orderByDesc(Announcement::getEffectiveTime);
        Page<Announcement> page = new Page<>(pageNum, pageSize);
        IPage<Announcement> result = announcementMapper.selectPage(page, wrapper);
        return PageResult.of(result);
    }

    /**
     * 公告是“发布者写给读者”的信息，不属于发布者的本人数据，
     * 因此不能沿用 {@code @DataScope} 的 create_by/villager_id 过滤。
     * <p>
     * 管理角色可查看全部公告；网格员仅看全村公告、本网格定向公告及本人发布的公告；
     * 村民等只读角色仅能查看已发布、已生效且面向全村或本网格的公告。
     */
    private void applyVisibility(LambdaQueryWrapper<Announcement> wrapper) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            wrapper.apply("1 = 0");
            return;
        }
        if (loginUser.isAdmin() || loginUser.getRoles().contains("COMMITTEE")) {
            return;
        }

        Long gridId = loginUser.getGridId();
        if (loginUser.getRoles().contains("GRID_WORKER")) {
            if (gridId == null) {
                return;
            }
            wrapper.and(w -> w.eq(Announcement::getPublishType, 1)
                    .or(x -> x.eq(Announcement::getPublishType, 2).eq(Announcement::getGridId, gridId))
                    .or(x -> x.eq(Announcement::getPublishBy, loginUser.getUserId())));
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        wrapper.eq(Announcement::getStatus, 2)
                .and(w -> w.isNull(Announcement::getEffectiveTime)
                        .or().le(Announcement::getEffectiveTime, now))
                .and(w -> w.isNull(Announcement::getExpireTime)
                        .or().gt(Announcement::getExpireTime, now));
        if (gridId == null) {
            wrapper.eq(Announcement::getPublishType, 1);
        } else {
            wrapper.and(w -> w.eq(Announcement::getPublishType, 1)
                    .or(x -> x.eq(Announcement::getPublishType, 2).eq(Announcement::getGridId, gridId)));
        }
    }

    @Override
    @Transactional
    public Announcement create(AnnouncementRequest request) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Announcement announcement = new Announcement();
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setCategory(request.getCategory() == null ? "通知公告" : request.getCategory());
        announcement.setPublishType(request.getPublishType() == null ? 1 : request.getPublishType());
        announcement.setGridId(request.getGridId());
        announcement.setIsTop(request.getIsTop() == null ? 0 : request.getIsTop());
        announcement.setEffectiveTime(request.getEffectiveTime());
        announcement.setStatus(request.getStatus() == null ? 2 : request.getStatus());
        announcement.setPublishBy(loginUser.getUserId());
        announcement.setReadCount(0);
        announcementMapper.insert(announcement);
        return announcement;
    }

    @Override
    @Transactional
    public Announcement update(Long id, AnnouncementRequest request) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Announcement announcement = getById(id);
        checkEditable(announcement, loginUser);

        Integer status = request.getStatus() == null ? announcement.getStatus() : request.getStatus();
        String category = request.getCategory() == null ? "通知公告" : request.getCategory();
        Integer publishType = request.getPublishType() == null ? 1 : request.getPublishType();
        Integer isTop = request.getIsTop() == null ? 0 : request.getIsTop();

        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setCategory(category);
        announcement.setPublishType(publishType);
        announcement.setGridId(request.getGridId());
        announcement.setIsTop(isTop);
        announcement.setEffectiveTime(request.getEffectiveTime());
        announcement.setStatus(status);

        announcementMapper.update(null, new LambdaUpdateWrapper<Announcement>()
                .eq(Announcement::getId, id)
                .set(Announcement::getTitle, request.getTitle())
                .set(Announcement::getContent, request.getContent())
                .set(Announcement::getCategory, category)
                .set(Announcement::getPublishType, publishType)
                .set(Announcement::getGridId, request.getGridId())
                .set(Announcement::getIsTop, isTop)
                .set(Announcement::getEffectiveTime, request.getEffectiveTime())
                .set(Announcement::getStatus, status)
                .set(Announcement::getUpdateBy, loginUser.getUserId())
                .set(Announcement::getUpdateTime, LocalDateTime.now()));
        return announcement;
    }

    @Override
    @Transactional
    public void withdraw(Long id) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Announcement announcement = getById(id);
        if (announcement.getStatus() == null || announcement.getStatus() != 2) {
            throw new BusinessException("仅已发布公告可下架");
        }
        boolean manager = loginUser.isAdmin() || loginUser.getRoles().contains("COMMITTEE");
        if (!manager && !Objects.equals(announcement.getPublishBy(), loginUser.getUserId())) {
            throw new BusinessException("只能下架本人发布的公告");
        }
        announcementMapper.update(null, new LambdaUpdateWrapper<Announcement>()
                .eq(Announcement::getId, id)
                .set(Announcement::getStatus, 3)
                .set(Announcement::getUpdateBy, loginUser.getUserId())
                .set(Announcement::getUpdateTime, LocalDateTime.now()));
    }

    /** 已发布公告需先下架再编辑；已归档公告不可编辑；网格员只能编辑本人发布的公告。 */
    private void checkEditable(Announcement announcement, LoginUser loginUser) {
        Integer status = announcement.getStatus();
        if (status != null && status == 2) {
            throw new BusinessException("已发布公告请先下架后再编辑");
        }
        if (status != null && status == 4) {
            throw new BusinessException("已归档公告不可编辑");
        }
        boolean manager = loginUser.isAdmin() || loginUser.getRoles().contains("COMMITTEE");
        if (!manager && !Objects.equals(announcement.getPublishBy(), loginUser.getUserId())) {
            throw new BusinessException("只能编辑本人发布的公告");
        }
    }

    private Announcement getById(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "公告不存在");
        }
        return announcement;
    }
}
