package com.rural.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.rural.datascope.DataScope;
import com.rural.entity.ServiceApply;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ServiceApplyMapper extends BaseMapper<ServiceApply> {

    @DataScope(viaVillager = true, selfColumn = "villager_id")
    @Select("SELECT * FROM service_apply")
    List<ServiceApply> selectByScope();

    @DataScope(viaVillager = true, selfColumn = "villager_id")
    @Select("SELECT * FROM service_apply ${ew.customSqlSegment}")
    IPage<ServiceApply> selectPageByScope(IPage<ServiceApply> page, @Param("ew") Wrapper<ServiceApply> wrapper);
}
