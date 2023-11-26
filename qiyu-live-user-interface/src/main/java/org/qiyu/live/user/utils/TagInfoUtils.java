package org.qiyu.live.user.utils;

public class TagInfoUtils {

    /**
     * Determine if a tag exists
     * @param tagInfo user current tag value
     * @param matchTag tag to find
     * @return
     */
    public static boolean isContain(Long tagInfo, Long matchTag) {
        return tagInfo != null && matchTag != null && matchTag > 0 && (tagInfo & matchTag) == matchTag;
    }
}
