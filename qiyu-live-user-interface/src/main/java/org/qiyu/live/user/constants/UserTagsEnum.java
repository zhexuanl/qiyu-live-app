package org.qiyu.live.user.constants;

import lombok.Getter;

@Getter
public enum UserTagsEnum {

    IS_RICH((long) Math.pow(2, 0), "is rich user", "tag_info_01"),
    IS_VIP((long) Math.pow(2, 1), "is VIP user", "tag_info_01"),
    IS_OLD_USER((long) Math.pow(2, 2), "Is old user", "tag_info_01");

    private final long tag;
    private final String desc;
    private final String fieldName;


    UserTagsEnum(long tag, String desc, String fieldName) {
        this.tag = tag;
        this.desc = desc;
        this.fieldName = fieldName;
    }
}
