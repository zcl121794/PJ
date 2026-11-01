package com.rural.service;

import com.rural.common.PageResult;
import com.rural.dto.ImportResult;
import com.rural.dto.VillagerRequest;
import com.rural.entity.Villager;
import org.springframework.web.multipart.MultipartFile;

public interface VillagerService {

    PageResult<Villager> list(int pageNum, int pageSize, String name, String idCard, String specialTag);

    Villager create(VillagerRequest request);

    Villager update(Long id, VillagerRequest request);

    void delete(Long id);

    ImportResult importVillagers(MultipartFile file);
}
