package org.qiyu.live.user.provider.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.qiyu.live.user.provider.dao.po.UserTagPO;

@Mapper
public interface IUserTagMapper extends BaseMapper<UserTagPO> {

    @Update("update t_user_tag set ${fieldName}=${fieldName} | #{tag} where user_id=#{userId}")
    int setTag(@Param("userId") Long userId, @Param("fieldName") String fieldName, @Param("tag") long tag);

    @Update("update t_user_tag set ${fieldName}=${fieldName} &~ #{tag} where user_id=#{userId}")
    int cancelTag(@Param("userId") Long userId, @Param("fieldName") String fieldName, @Param("tag") long tag);
}
