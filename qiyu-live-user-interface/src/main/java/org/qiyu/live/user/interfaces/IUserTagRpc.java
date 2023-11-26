package org.qiyu.live.user.interfaces;

import org.qiyu.live.user.constants.UserTagsEnum;

public interface IUserTagRpc {

    boolean setTag(Long userId, UserTagsEnum userTagEnum);

    boolean cancelTag(Long userId, UserTagsEnum userTagEnum);

    boolean containTag(Long userId, UserTagsEnum userTagEnum);
}
