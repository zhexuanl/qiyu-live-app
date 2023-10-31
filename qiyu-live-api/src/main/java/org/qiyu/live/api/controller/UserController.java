package org.qiyu.live.api.controller;

import org.apache.dubbo.config.annotation.DubboReference;
import org.qiyu.live.user.dto.UserDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.qiyu.live.user.interfaces.IUserRpc;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/user")
public class UserController {

    @DubboReference
    IUserRpc userRpc;

    @GetMapping("/getUserInfo")
    public UserDTO getUserInfo(@RequestParam Long userId) {
        return userRpc.getByUserId(userId);
    }

    @GetMapping("/updateUserInfo")
    public boolean updateUserInfo(@RequestParam Long userId, @RequestParam String nickName) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(userId);
        userDTO.setNickName(nickName);
        return userRpc.updateUserInfo(userDTO);
    }

    @GetMapping("/insertOne")
    public boolean insertOne(@RequestParam Long userId) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(userId);
        userDTO.setSex(1);
        userDTO.setNickName("test");
        return userRpc.insertOne(userDTO);
    }

    @GetMapping("/batchQueryUserInfo")
    public Map<Long, UserDTO> batchQueryUserInfo(String userIdStr) {
        return userRpc.batchQueryUserInfo(Arrays.stream(userIdStr.split(",")).map(Long::valueOf).collect(Collectors.toList()));
    }
}
