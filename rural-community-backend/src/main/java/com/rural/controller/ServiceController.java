package com.rural.controller;

import com.rural.common.PageResult;
import com.rural.common.Result;
import com.rural.dto.ServiceApplyRequest;
import com.rural.dto.ServiceHandleRequest;
import com.rural.dto.ServiceRejectRequest;
import com.rural.entity.ServiceApply;
import com.rural.entity.ServiceItem;
import com.rural.service.ServiceService;
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
@RequestMapping("/service")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceService serviceService;

    @GetMapping("/item/list")
    @PreAuthorize("hasAuthority('service:guide')")
    public Result<List<ServiceItem>> listItems() {
        return Result.success(serviceService.listItems());
    }

    @GetMapping("/item/{id}")
    @PreAuthorize("hasAuthority('service:guide')")
    public Result<ServiceItem> itemDetail(@PathVariable Long id) {
        return Result.success(serviceService.itemDetail(id));
    }

    @GetMapping("/apply/list")
    @PreAuthorize("hasAuthority('service:apply')")
    public Result<PageResult<ServiceApply>> listApplies(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(serviceService.listApplies(pageNum, pageSize));
    }

    @GetMapping("/apply/{id}")
    @PreAuthorize("hasAuthority('service:apply')")
    public Result<ServiceApply> applyDetail(@PathVariable Long id) {
        return Result.success(serviceService.applyDetail(id));
    }

    @PostMapping("/apply")
    @PreAuthorize("hasAuthority('service:apply')")
    public Result<ServiceApply> apply(@Validated @RequestBody ServiceApplyRequest request) {
        return Result.success(serviceService.apply(request));
    }

    @PutMapping("/apply/{id}/accept")
    @PreAuthorize("hasAuthority('service:accept')")
    public Result<Void> accept(@PathVariable Long id) {
        serviceService.accept(id);
        return Result.success();
    }

    /** 网格员初审通过 */
    @PutMapping("/apply/{id}/grid-pass")
    @PreAuthorize("hasAuthority('service:audit')")
    public Result<Void> gridPass(@PathVariable Long id, @RequestBody(required = false) ServiceHandleRequest request) {
        serviceService.gridPass(id, request);
        return Result.success();
    }

    /** 网格员初审不通过 */
    @PutMapping("/apply/{id}/grid-reject")
    @PreAuthorize("hasAuthority('service:audit')")
    public Result<Void> gridReject(@PathVariable Long id, @Validated @RequestBody ServiceRejectRequest request) {
        serviceService.gridReject(id, request);
        return Result.success();
    }

    @PutMapping("/apply/{id}/finish")
    @PreAuthorize("hasAuthority('service:finish')")
    public Result<Void> finish(@PathVariable Long id, @RequestBody ServiceHandleRequest request) {
        serviceService.finish(id, request);
        return Result.success();
    }

    /** 村委审核通过 */
    @PutMapping("/apply/{id}/approve")
    @PreAuthorize("hasAuthority('service:audit')")
    public Result<Void> approve(@PathVariable Long id) {
        serviceService.approve(id);
        return Result.success();
    }

    /** 村委驳回申请 */
    @PutMapping("/apply/{id}/reject")
    @PreAuthorize("hasAuthority('service:audit')")
    public Result<Void> reject(@PathVariable Long id, @Validated @RequestBody ServiceRejectRequest request) {
        serviceService.reject(id, request);
        return Result.success();
    }
}
