package com.rural.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rural.entity.SysUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT sr.role_code FROM `sys_role` sr " +
            "JOIN `sys_user_role` sur ON sr.id = sur.role_id " +
            "WHERE sur.user_id = #{userId} AND sr.deleted = 0 AND sr.status = 1")
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    @Select("SELECT DISTINCT sm.perms FROM `sys_menu` sm " +
            "JOIN `sys_role_menu` srm ON sm.id = srm.menu_id " +
            "JOIN `sys_user_role` sur ON srm.role_id = sur.role_id " +
            "WHERE sur.user_id = #{userId} AND sm.perms IS NOT NULL AND sm.perms != '' " +
            "AND sm.deleted = 0 AND sm.status = 1")
    List<String> selectPermsByUserId(@Param("userId") Long userId);

    /** 可被分派诉求的用户：网格员、村委工作人员 */
    @Select("SELECT DISTINCT u.* FROM `sys_user` u " +
            "JOIN `sys_user_role` sur ON sur.user_id = u.id " +
            "JOIN `sys_role` sr ON sr.id = sur.role_id " +
            "WHERE u.status = 1 AND u.deleted = 0 " +
            "AND sr.status = 1 AND sr.deleted = 0 " +
            "AND sr.role_code IN ('GRID_WORKER', 'COMMITTEE') " +
            "ORDER BY u.id")
    List<SysUser> selectAssignableProcessors();
}
