package com.rural.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.rural.datascope.DataScope;
import com.rural.entity.Appeal;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AppealMapper extends BaseMapper<Appeal> {

    @DataScope(viaVillager = true, selfColumn = "villager_id")
    @Select("SELECT * FROM appeal")
    List<Appeal> selectByScope();

    @DataScope(viaVillager = true, selfColumn = "villager_id")
    @Select("SELECT * FROM appeal ${ew.customSqlSegment}")
    IPage<Appeal> selectPageByScope(IPage<Appeal> page, @Param("ew") Wrapper<Appeal> wrapper);
}
