package com.rural.datascope;

/**
 * 三级数据权限（外加村民本人数据）
 */
public enum DataScopeType {

    ALL("全网数据"),
    VILLAGE("本村数据"),
    GRID("本网格数据"),
    SELF("本人数据");

    private final String desc;

    DataScopeType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
