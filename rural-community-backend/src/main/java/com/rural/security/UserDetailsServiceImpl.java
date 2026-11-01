package com.rural.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rural.datascope.DataScopeType;
import com.rural.entity.SysUser;
import com.rural.entity.Villager;
import com.rural.mapper.SysUserMapper;
import com.rural.mapper.VillagerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;
    private final VillagerMapper villagerMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new UsernameNotFoundException("账号已停用");
        }

        Set<String> roles = new HashSet<>(sysUserMapper.selectRoleCodesByUserId(user.getId()));
        Set<String> permissions = new HashSet<>(sysUserMapper.selectPermsByUserId(user.getId()));
        DataScopeType dataScope = resolveDataScope(roles);
        Long villagerId = dataScope == DataScopeType.SELF ? resolveVillagerId(user) : null;

        return new LoginUser(user, roles, permissions, dataScope, villagerId);
    }

    private DataScopeType resolveDataScope(Set<String> roles) {
        if (roles.contains("ADMIN")) {
            return DataScopeType.ALL;
        }
        if (roles.contains("COMMITTEE")) {
            return DataScopeType.VILLAGE;
        }
        if (roles.contains("GRID_WORKER")) {
            return DataScopeType.GRID;
        }
        return DataScopeType.SELF;
    }

    private Long resolveVillagerId(SysUser user) {
        Villager villager = villagerMapper.selectOne(
                new LambdaQueryWrapper<Villager>().eq(Villager::getUserId, user.getId()));
        return villager == null ? null : villager.getId();
    }
}
