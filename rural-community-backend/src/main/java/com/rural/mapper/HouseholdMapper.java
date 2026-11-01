package com.rural.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rural.datascope.DataScope;
import com.rural.entity.Household;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface HouseholdMapper extends BaseMapper<Household> {

    @DataScope
    @Select("SELECT * FROM household")
    List<Household> selectByScope();
}
