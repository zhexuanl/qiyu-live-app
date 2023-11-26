package org.qiyu.live.user.provider.rpc;

import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.qiyu.live.user.dto.UserDTO;
import org.qiyu.live.user.interfaces.IUserRpc;
import org.qiyu.live.user.provider.service.UserService;

import java.util.List;
import java.util.Map;

@DubboService
@RequiredArgsConstructor
public class UserRpcImpl implements IUserRpc {

    private final UserService userService;

    @Override
    public String test() {
        System.out.println("This is dubbo test");
        return "success";
    }

    @Override
    public UserDTO getByUserId(Long userId) {
        return userService.getByUserId(userId);
    }

    @Override
    public boolean updateUserInfo(UserDTO userDTO) {
        return userService.updateUserInfo(userDTO);
    }

    public boolean insertOne(UserDTO userDTO) {
        return userService.insertOne(userDTO);
    }

    @Override
    public Map<Long, UserDTO> batchQueryUserInfo(List<Long> userIdList) {
        return userService.batchQueryUserInfo(userIdList);
    }
}
