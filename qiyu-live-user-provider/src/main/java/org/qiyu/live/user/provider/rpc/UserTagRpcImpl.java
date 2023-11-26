package org.qiyu.live.user.provider.rpc;

import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.qiyu.live.user.constants.UserTagsEnum;
import org.qiyu.live.user.interfaces.IUserTagRpc;
import org.qiyu.live.user.provider.service.UserTagService;


@DubboService
@RequiredArgsConstructor
public class UserTagRpcImpl implements IUserTagRpc {

    private final UserTagService userTagService;

    @Override
    public boolean setTag(Long userId, UserTagsEnum userTagEnum) {
        return userTagService.setTag(userId, userTagEnum);
    }

    @Override
    public boolean cancelTag(Long userId, UserTagsEnum userTagEnum) {
        return userTagService.cancelTag(userId, userTagEnum);
    }

    @Override
    public boolean containTag(Long userId, UserTagsEnum userTagEnum) {
        return userTagService.containTag(userId, userTagEnum);
    }
}
