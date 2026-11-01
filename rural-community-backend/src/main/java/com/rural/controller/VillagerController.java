package com.rural.controller;

import com.rural.common.PageResult;
import com.rural.common.Result;
import com.rural.dto.ImportResult;
import com.rural.dto.VillagerRequest;
import com.rural.entity.Villager;
import com.rural.service.VillagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/villager")
@RequiredArgsConstructor
public class VillagerController {

    private final VillagerService villagerService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('villager:list')")
    public Result<PageResult<Villager>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String idCard,
            @RequestParam(required = false) String specialTag) {
        return Result.success(villagerService.list(pageNum, pageSize, name, idCard, specialTag));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('villager:add')")
    public Result<Villager> create(@Validated @RequestBody VillagerRequest request) {
        return Result.success(villagerService.create(request));
    }

    @PostMapping("/import")
    @PreAuthorize("hasAuthority('villager:import')")
    public Result<ImportResult> importVillagers(@RequestParam("file") MultipartFile file) {
        return Result.success(villagerService.importVillagers(file));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('villager:edit')")
    public Result<Villager> update(@PathVariable Long id, @Validated @RequestBody VillagerRequest request) {
        return Result.success(villagerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('villager:remove')")
    public Result<Void> delete(@PathVariable Long id) {
        villagerService.delete(id);
        return Result.success();
    }
}
