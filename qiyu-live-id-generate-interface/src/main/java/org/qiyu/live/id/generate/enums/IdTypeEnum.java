package org.qiyu.live.id.generate.enums;


public enum IdTypeEnum {

    USER_ID(1, "User Id Generation Strategy");

    int code;
    String desc;

    IdTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
