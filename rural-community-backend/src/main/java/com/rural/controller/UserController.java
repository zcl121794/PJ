package com.rural.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rural.common.Result;
import com.rural.dto.UserCreateRequest;
import com.rural.entity.SysUser;
import com.rural.mapper.SysUserMapper;
import com.rural.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final SysUserMapper sysUserMapper;
    private final UserService userService;

    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('system:user', 'appeal:assign')")
    public Result<List<SysUser>> list() {
        return Result.success(sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getStatus, 1)
                        .orderByAsc(SysUser::getId)));
    }

    /** 诉求分派可选处理人：仅网格员与村委工作人员 */
    @GetMapping("/assignable")
    @PreAuthorize("hasAnyAuthority('system:user', 'appeal:assign')")
    public Result<List<SysUser>> assignable() {
        return Result.success(sysUserMapper.selectAssignableProcessors());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:user')")
    public Result<SysUser> create(@Validated @RequestBody UserCreateRequest request) {
        return Result.success(userService.create(request));
    }
}
