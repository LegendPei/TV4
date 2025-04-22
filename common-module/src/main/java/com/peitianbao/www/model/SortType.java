package com.peitianbao.www.model;

import lombok.Getter;

/**
 * @author leg
 */
@Getter
public enum SortType {
    //默认按Id排序
    DEFAULT("id"),
    //按点赞数排序
    LIKES("likes"),
    //按关注数排序
    FOLLOWERS("followers");

    private final String value;

    SortType(String value) {
        this.value = value;
    }

    /**
     * 根据字符串值获取对应的枚举
     */
    public static SortType fromValue(String value) {
        for (SortType sortType : values()) {
            if (sortType.getValue().equalsIgnoreCase(value)) {
                return sortType;
            }
        }
        return DEFAULT;
    }
}