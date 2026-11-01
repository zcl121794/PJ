package com.rural.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rural.common.Result;
import com.rural.entity.SysDict;
import com.rural.mapper.SysDictMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dict")
@RequiredArgsConstructor
public class DictController {

    private final SysDictMapper sysDictMapper;

    @GetMapping("/type/{type}")
    public Result<List<SysDict>> listByType(@PathVariable String type) {
        return Result.success(sysDictMapper.selectList(
                new LambdaQueryWrapper<SysDict>()
                        .eq(SysDict::getDictType, type)
                        .eq(SysDict::getStatus, 1)
                        .orderByAsc(SysDict::getSort)));
    }
}
