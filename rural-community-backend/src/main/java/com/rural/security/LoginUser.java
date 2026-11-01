package com.rural.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rural.datascope.DataScopeType;
import com.rural.entity.SysUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Getter
public class LoginUser implements UserDetails {

    private final SysUser user;
    private final Set<String> roles;
    private final Set<String> permissions;
    private final DataScopeType dataScope;
    private final Long villagerId;

    public LoginUser(SysUser user, Set<String> roles, Set<String> permissions,
                     DataScopeType dataScope, Long villagerId) {
        this.user = user;
        this.roles = roles;
        this.permissions = permissions;
        this.dataScope = dataScope;
        this.villagerId = villagerId;
    }

    public boolean isAdmin() {
        return roles != null && roles.contains("ADMIN");
    }

    public Long getUserId() {
        return user.getId();
    }

    public Long getGridId() {
        return user.getGridId();
    }

    public Long getVillageId() {
        return user.getVillageId();
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r)));
        permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
        return authorities;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return user.getStatus() != null && user.getStatus() == 1;
    }
}
