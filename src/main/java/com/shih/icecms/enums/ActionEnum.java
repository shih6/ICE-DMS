package com.shih.icecms.enums;

public enum ActionEnum {
    ACCESS_CONTROL(31);
    private final Integer desc;

    ActionEnum(Integer desc) {
        this.desc = desc;
    }

    public Integer getDesc() {
        return desc;
    }
}
