package com.rural.datascope;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据权限注解，标注在 Mapper 方法上，声明该查询需按当前用户数据范围过滤。
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {

    /** 网格字段（表直接包含 grid_id 时） */
    String gridColumn() default "grid_id";

    /** 村字段（表直接包含 village_id 时） */
    String villageColumn() default "village_id";

    /** 本人数据字段（SELF 级别），如 create_by 或 villager_id */
    String selfColumn() default "create_by";

    /** GRID 级别：通过村民表关联（appeal/service_apply 经 villager_id 关联） */
    boolean viaVillager() default false;

    /** GRID 级别：通过户表关联（villager 经 household_id 关联） */
    boolean viaHousehold() default false;
}
