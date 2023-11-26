package org.qiyu.live.user.provider.service;

import org.qiyu.live.user.constants.UserTagsEnum;

public interface UserTagService {

    boolean setTag(Long userId, UserTagsEnum userTagEnum);

    boolean cancelTag(Long userId, UserTagsEnum userTagEnum);

    boolean containTag(Long userId, UserTagsEnum userTagEnum);
}
