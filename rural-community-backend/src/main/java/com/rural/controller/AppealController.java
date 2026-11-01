package com.rural.controller;

import com.rural.common.PageResult;
import com.rural.common.Result;
import com.rural.dto.AppealAssignRequest;
import com.rural.dto.AppealDissatisfiedRequest;
import com.rural.dto.AppealFollowUpRequest;
import com.rural.dto.AppealHandleRequest;
import com.rural.dto.AppealReportRequest;
import com.rural.dto.AppealRequest;
import com.rural.dto.AppealReturnRequest;
import com.rural.dto.AppealUrgeRequest;
import com.rural.entity.Appeal;
import com.rural.entity.AppealFlow;
import com.rural.service.AppealService;
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

import java.util.List;

@RestController
@RequestMapping("/appeal")
@RequiredArgsConstructor
public class AppealController {

    private final AppealService appealService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('appeal:list')")
    public Result<PageResult<Appeal>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        return Result.success(appealService.list(pageNum, pageSize, category, keyword));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('appeal:list')")
    public Result<Appeal> detail(@PathVariable Long id) {
        return Result.success(appealService.detail(id));
    }

    @GetMapping("/{id}/flows")
    @PreAuthorize("hasAuthority('appeal:list')")
    public Result<List<AppealFlow>> flows(@PathVariable Long id) {
        return Result.success(appealService.flows(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('appeal:list')")
    public Result<Appeal> submit(@Validated @RequestBody AppealRequest request) {
        return Result.success(appealService.submit(request));
    }

    @PutMapping("/{id}/accept")
    @PreAuthorize("hasAuthority('appeal:accept')")
    public Result<Void> accept(@PathVariable Long id) {
        appealService.accept(id);
        return Result.success();
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('appeal:assign')")
    public Result<Void> assign(@PathVariable Long id, @Validated @RequestBody AppealAssignRequest request) {
        appealService.assign(id, request);
        return Result.success();
    }

    @PutMapping("/{id}/handle")
    @PreAuthorize("hasAuthority('appeal:handle')")
    public Result<Void> handle(@PathVariable Long id, @Validated @RequestBody AppealHandleRequest request) {
        appealService.handle(id, request);
        return Result.success();
    }

    /** 网格员上报村委处理 */
    @PutMapping("/{id}/report")
    @PreAuthorize("hasAuthority('appeal:handle')")
    public Result<Void> reportToCommittee(@PathVariable Long id,
                                          @RequestBody(required = false) AppealReportRequest request) {
        appealService.reportToCommittee(id, request);
        return Result.success();
    }

    /** 村委审核通过 */
    @PutMapping("/{id}/finish")
    @PreAuthorize("hasAuthority('appeal:finish')")
    public Result<Void> finish(@PathVariable Long id) {
        appealService.finish(id);
        return Result.success();
    }

    /** 村委审核退回并说明理由 */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('appeal:finish')")
    public Result<Void> rejectHandling(@PathVariable Long id, @Validated @RequestBody AppealReturnRequest request) {
        appealService.rejectHandling(id, request);
        return Result.success();
    }

    /** 村委催办处理中的诉求 */
    @PutMapping("/{id}/urge")
    @PreAuthorize("hasAuthority('appeal:handle')")
    public Result<Void> urge(@PathVariable Long id, @RequestBody(required = false) AppealUrgeRequest request) {
        appealService.urge(id, request);
        return Result.success();
    }

    /** 村委修改处理结果 */
    @PutMapping("/{id}/result")
    @PreAuthorize("hasAuthority('appeal:handle')")
    public Result<Void> updateProcessResult(@PathVariable Long id, @Validated @RequestBody AppealHandleRequest request) {
        appealService.updateProcessResult(id, request);
        return Result.success();
    }

    /** 村民反馈：满意 */
    @PutMapping("/{id}/satisfied")
    @PreAuthorize("hasAuthority('appeal:list')")
    public Result<Void> satisfied(@PathVariable Long id) {
        appealService.satisfied(id);
        return Result.success();
    }

    /** 村民反馈：不满意并说明理由 */
    @PutMapping("/{id}/dissatisfied")
    @PreAuthorize("hasAuthority('appeal:list')")
    public Result<Void> dissatisfied(@PathVariable Long id,
                                     @Validated @RequestBody AppealDissatisfiedRequest request) {
        appealService.dissatisfied(id, request);
        return Result.success();
    }

    /** 村民反馈：追问问题 */
    @PutMapping("/{id}/follow-up")
    @PreAuthorize("hasAuthority('appeal:list')")
    public Result<Void> followUp(@PathVariable Long id,
                                 @Validated @RequestBody AppealFollowUpRequest request) {
        appealService.followUp(id, request);
        return Result.success();
    }
}
