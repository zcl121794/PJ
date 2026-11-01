package com.rural.datascope;

import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import com.rural.security.LoginUser;
import com.rural.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;

import java.lang.reflect.Method;

/**
 * 数据权限 SQL 片段处理器。
 * 依据当前登录用户的数据范围（三级 + 本人）为标注了 {@link DataScope} 的查询拼接过滤条件。
 */
@Slf4j
public class DataScopeHandler implements DataPermissionHandler {

    @Override
    public Expression getSqlSegment(Expression where, String mappedStatementId) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || loginUser.isAdmin()) {
            return null;
        }
        DataScope dataScope = resolveDataScope(mappedStatementId);
        if (dataScope == null) {
            return null;
        }
        String condition = buildCondition(loginUser, dataScope);
        if (condition == null) {
            return null;
        }
        try {
            return CCJSqlParserUtil.parseCondExpression(condition);
        } catch (Exception e) {
            log.error("数据权限条件解析失败: {}", condition, e);
            return null;
        }
    }

    private DataScope resolveDataScope(String mappedStatementId) {
        int lastDot = mappedStatementId.lastIndexOf('.');
        if (lastDot < 0) {
            return null;
        }
        String interfaceName = mappedStatementId.substring(0, lastDot);
        String methodName = mappedStatementId.substring(lastDot + 1);
        try {
            Class<?> mapperClass = Class.forName(interfaceName);
            for (Method method : mapperClass.getDeclaredMethods()) {
                if (method.getName().equals(methodName)) {
                    return method.getAnnotation(DataScope.class);
                }
            }
        } catch (ClassNotFoundException e) {
            log.debug("未找到 Mapper 类: {}", interfaceName);
        }
        return null;
    }

    private String buildCondition(LoginUser user, DataScope ds) {
        switch (user.getDataScope()) {
            case ALL:
                return null;
            case VILLAGE:
                // 单村试点场景：业务表未冗余 village_id 字段，本村数据即全部数据，无需过滤。
                // 多村扩展时需在业务表冗余 village_id 字段，再启用：
                //   return ds.villageColumn() + " = " + user.getVillageId();
                return null;
            case GRID:
                if (user.getGridId() == null) {
                    return null;
                }
                if (ds.viaVillager()) {
                    return "villager_id IN (SELECT v.id FROM villager v JOIN household h ON v.household_id = h.id WHERE h.grid_id = "
                            + user.getGridId() + ")";
                }
                if (ds.viaHousehold()) {
                    return "household_id IN (SELECT h.id FROM household h WHERE h.grid_id = "
                            + user.getGridId() + ")";
                }
                return ds.gridColumn() + " = " + user.getGridId();
            case SELF:
                if ("villager_id".equals(ds.selfColumn())) {
                    if (user.getVillagerId() == null) {
                        return "1 = 0";
                    }
                    return "villager_id = " + user.getVillagerId();
                }
                return ds.selfColumn() + " = " + user.getUserId();
            default:
                return null;
        }
    }
}
