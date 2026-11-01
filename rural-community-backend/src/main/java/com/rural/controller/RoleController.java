package com.rural.controller;

import com.rural.common.Result;
import com.rural.entity.SysRole;
import com.rural.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {

    private final SysRoleMapper sysRoleMapper;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:user')")
    public Result<List<SysRole>> list() {
        return Result.success(sysRoleMapper.selectList(null));
    }
}
