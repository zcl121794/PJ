package com.rural.controller;

import com.rural.common.PageResult;
import com.rural.common.Result;
import com.rural.dto.AnnouncementRequest;
import com.rural.entity.Announcement;
import com.rural.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('announcement:list')")
    public Result<PageResult<Announcement>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return Result.success(announcementService.list(pageNum, pageSize, keyword, category));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('announcement:add')")
    public Result<Announcement> create(@Validated @RequestBody AnnouncementRequest request) {
        return Result.success(announcementService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('announcement:edit')")
    public Result<Announcement> update(@PathVariable Long id, @Validated @RequestBody AnnouncementRequest request) {
        return Result.success(announcementService.update(id, request));
    }

    @PutMapping("/{id}/withdraw")
    @PreAuthorize("hasAuthority('announcement:remove')")
    public Result<Void> withdraw(@PathVariable Long id) {
        announcementService.withdraw(id);
        return Result.success();
    }
}
