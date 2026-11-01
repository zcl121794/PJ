package com.rural.controller;

import com.rural.common.Result;
import com.rural.entity.Household;
import com.rural.mapper.HouseholdMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/household")
@RequiredArgsConstructor
public class HouseholdController {

    private final HouseholdMapper householdMapper;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('villager:add')")
    public Result<List<Household>> list() {
        return Result.success(householdMapper.selectByScope());
    }
}
