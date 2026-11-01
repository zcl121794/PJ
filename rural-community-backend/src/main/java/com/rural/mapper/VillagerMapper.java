package com.rural.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.rural.datascope.DataScope;
import com.rural.entity.Villager;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface VillagerMapper extends BaseMapper<Villager> {

    @DataScope(viaHousehold = true)
    @Select("SELECT * FROM villager")
    List<Villager> selectByScope();

    @DataScope(viaHousehold = true)
    @Select("SELECT * FROM villager ${ew.customSqlSegment}")
    IPage<Villager> selectPageByScope(IPage<Villager> page, @Param("ew") Wrapper<Villager> wrapper);
}
