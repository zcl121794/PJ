package com.rural.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ImportResult {

    private int success;

    private int fail;

    private List<String> errors = new ArrayList<>();
}
