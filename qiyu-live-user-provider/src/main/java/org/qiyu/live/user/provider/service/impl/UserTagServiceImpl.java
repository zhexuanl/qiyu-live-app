package org.qiyu.live.user.provider.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qiyu.live.user.constants.UserTagsEnum;
import org.qiyu.live.user.provider.dao.mapper.IUserTagMapper;
import org.qiyu.live.user.provider.dao.po.UserTagPO;
import org.qiyu.live.user.provider.service.UserTagService;
import org.qiyu.live.user.utils.TagInfoUtils;
import org.springframework.stereotype.Service;

import static org.qiyu.live.user.constants.UserTagFieldNameConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserTagServiceImpl implements UserTagService {

    private final IUserTagMapper userTagMapper;

    @Override
    public boolean setTag(Long userId, UserTagsEnum userTagEnum) {
        return userTagMapper.setTag(userId, userTagEnum.getFieldName(), userTagEnum.getTag()) > 0;
    }

    @Override
    public boolean cancelTag(Long userId, UserTagsEnum userTagEnum) {
        return userTagMapper.cancelTag(userId, userTagEnum.getFieldName(), userTagEnum.getTag()) > 0;
    }

    @Override
    public boolean containTag(Long userId, UserTagsEnum userTagEnum) {
        UserTagPO userTagPO = userTagMapper.selectById(userId);
        log.info("call DB userTagPO: {}", userTagPO);

        if (userTagPO == null) {
            return false;
        }
        String queryFieldName = userTagEnum.getFieldName();

        if (FIELD_01.equals(queryFieldName)) {
            return TagInfoUtils.isContain(userTagPO.getTagInfo01(), userTagEnum.getTag());
        } else if (FIELD_02.equals(queryFieldName)) {
            return TagInfoUtils.isContain(userTagPO.getTagInfo02(), userTagEnum.getTag());
        } else if (FIELD_03.equals(queryFieldName)) {
            return TagInfoUtils.isContain(userTagPO.getTagInfo03(), userTagEnum.getTag());
        }
        return false;
    }
}
