package com.rural.service;

import com.rural.common.PageResult;
import com.rural.dto.AnnouncementRequest;
import com.rural.entity.Announcement;

public interface AnnouncementService {

    PageResult<Announcement> list(int pageNum, int pageSize, String keyword, String category);

    Announcement create(AnnouncementRequest request);

    /** 编辑公告（草稿/待审核/已下架） */
    Announcement update(Long id, AnnouncementRequest request);

    /** 下架已发布公告 */
    void withdraw(Long id);
}
