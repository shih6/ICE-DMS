package com.shih.icedms.enums;

public enum ActionEnum {
    AccessControl(31),
    View(0b01000),
    Download(0b00100),
    Edit(0b00010),
    Delete(0b00001);

    private final int desc;

    ActionEnum(int desc) {
        this.desc = desc;
    }

    public int getDesc() {
        return desc;
    }
}
