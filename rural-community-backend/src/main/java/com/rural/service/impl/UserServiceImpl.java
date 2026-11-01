package com.rural.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rural.common.exception.BusinessException;
import com.rural.dto.UserCreateRequest;
import com.rural.entity.SysUser;
import com.rural.entity.SysUserRole;
import com.rural.mapper.SysUserMapper;
import com.rural.mapper.SysUserRoleMapper;
import com.rural.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public SysUser create(UserCreateRequest request) {
        Long count = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setGender(request.getGender() == null ? 0 : request.getGender());
        user.setIdCard(request.getIdCard());
        user.setGridId(request.getGridId());
        user.setIsAuth(1);
        user.setStatus(1);
        sysUserMapper.insert(user);

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(request.getRoleId());
        sysUserRoleMapper.insert(userRole);

        return user;
    }
}
