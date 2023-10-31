package org.qiyu.live.user.provider.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.qiyu.live.common.interfaces.utils.ConvertBeanUtils;
import org.qiyu.live.common.interfaces.utils.OldVersion;
import org.qiyu.live.framework.redis.starter.key.UserProviderCacheKeyBuilder;
import org.qiyu.live.framework.redis.starter.utils.RedisUtils;
import org.qiyu.live.user.dto.UserDTO;
import org.qiyu.live.user.provider.dao.mapper.IUserMapper;
import org.qiyu.live.user.provider.dao.po.UserPO;
import org.qiyu.live.user.provider.service.IUserService;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements IUserService {

    @Resource
    private IUserMapper userMapper;

    @Resource
    private RedisTemplate<String, UserDTO> redisTemplate;

    @Resource
    private UserProviderCacheKeyBuilder userProviderCacheKeyBuilder;

    @Resource
    private MQProducer mqProducer;

    @Override
    public UserDTO getByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        String key = userProviderCacheKeyBuilder.buildUserInfoKey(userId);
        UserDTO userDTO = redisTemplate.opsForValue().get(key);
        if (userDTO != null) {
            return userDTO;
        }
        userDTO = ConvertBeanUtils.convert(userMapper.selectById(userId), UserDTO.class);
        if (userDTO != null) {
            redisTemplate.opsForValue().set(key, userDTO, 30, TimeUnit.MINUTES);
        }
        return userDTO;
    }

    @Override
    public boolean updateUserInfo(UserDTO userDTO) {
        if (userDTO == null || userDTO.getUserId() == null) {
            return false;
        }
        userMapper.updateById(ConvertBeanUtils.convert(userDTO, UserPO.class));
        String key = userProviderCacheKeyBuilder.buildUserInfoKey(userDTO.getUserId());
        redisTemplate.delete(key);
        try {
            Message message = new Message();
            message.setTopic("user-update-cache");
            message.setBody(JSON.toJSONString(userDTO).getBytes());
            //delay time level, 1 = 1 second
            message.setDelayTimeLevel(1);
            mqProducer.send(message);
        } catch (MQBrokerException | RemotingException | InterruptedException | MQClientException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public boolean insertOne(UserDTO userDTO) {
        if (userDTO == null || userDTO.getUserId() == null) {
            return false;
        }
        UserPO userPO = ConvertBeanUtils.convert(userDTO, UserPO.class);
        userMapper.insert(userPO);
        return true;
    }

    @Override
    public Map<Long, UserDTO> batchQueryUserInfo(List<Long> userIdList) {
        // Early exit if an input list is empty or null
        if (CollectionUtils.isEmpty(userIdList)) {
            return Collections.emptyMap();
        }

        // Filter out user IDs less than or equal to 10,000
        userIdList = userIdList.stream().filter(id -> id > 10000).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userIdList)) {
            return Collections.emptyMap();
        }

        // Fetch user records from Redis
        List<String> keyList = userIdList.stream()
                .map(userProviderCacheKeyBuilder::buildUserInfoKey)
                .collect(Collectors.toList());
        List<UserDTO> redisResults = redisTemplate.opsForValue().multiGet(keyList);

        // Separate cache hits and misses
        List<UserDTO> userDTOListFromCache = new ArrayList<>();
        List<Long> userIdNotInCacheList = new ArrayList<>();

        for (int i = 0; i < redisResults.size(); i++) {
            if (redisResults.get(i) != null) {
                userDTOListFromCache.add(redisResults.get(i));
            } else {
                userIdNotInCacheList.add(userIdList.get(i));
            }
        }

        // If all user records are fetched from Redis, return them
        if (userDTOListFromCache.size() == userIdList.size()) {
            return userDTOListFromCache.stream().collect(Collectors.toMap(UserDTO::getUserId, Function.identity()));
        }

        // Fetch remaining user records from the database
        Map<Long, List<Long>> userIdMap = userIdNotInCacheList.stream()
                .collect(Collectors.groupingBy(userId -> userId % 100));

        List<UserDTO> dbQueryResult = Collections.synchronizedList(new ArrayList<>());
        userIdMap.values().parallelStream().forEach(queryUserIdList -> {
            dbQueryResult.addAll(ConvertBeanUtils.convertList(userMapper.selectBatchIds(queryUserIdList), UserDTO.class));
        });

        // Cache the newly fetched records in Redis
        if (!dbQueryResult.isEmpty()) {
            Map<String, UserDTO> saveCacheMap = dbQueryResult.stream()
                    .collect(Collectors.toMap(
                            userDTO -> userProviderCacheKeyBuilder.buildUserInfoKey(userDTO.getUserId()),
                            Function.identity()
                    ));
            redisTemplate.opsForValue().multiSet(saveCacheMap);
            redisTemplate.executePipelined(new SessionCallback<>() {
                @Override
                public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                    for (String redisKey : saveCacheMap.keySet()) {
                        operations.expire((K) redisKey, RedisUtils.createRandomExpireTime(), TimeUnit.MINUTES);
                    }
                    return null;
                }
            });
            userDTOListFromCache.addAll(dbQueryResult);
        }

        return userDTOListFromCache.stream().collect(Collectors.toMap(UserDTO::getUserId, Function.identity()));
    }

    @Deprecated
    @OldVersion(newerVersion = "batchQueryUserInfo")
    public Map<Long, UserDTO> batchQueryUserInfoOld(List<Long> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return Maps.newHashMap();
        }

        userIdList = userIdList.stream().filter(id -> id > 10000).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userIdList)) {
            return Maps.newHashMap();
        }
        //redis
        //Didn't recommend, poor performance when there are many records
        //userIdList.forEach(userId -> redisTemplate.opsForValue().get(userId));
        List<String> keyList = new ArrayList<>();
        userIdList.forEach(userId -> keyList.add(userProviderCacheKeyBuilder.buildUserInfoKey(userId)));

        List<UserDTO> userDTOList = new ArrayList<>(Objects.requireNonNull(redisTemplate.opsForValue().multiGet(keyList)).stream().filter(Objects::nonNull).toList());
        if (!CollectionUtils.isEmpty(userDTOList) && userDTOList.size() == userIdList.size()) {
            return userDTOList.stream().collect(Collectors.toMap(UserDTO::getUserId, v -> v));
        }

        List<Long> userIdInCacheList = userDTOList.stream().map(UserDTO::getUserId).toList();
        List<Long> userIdNotInCacheList = userIdList.stream().filter(x -> !userIdInCacheList.contains(x)).toList();

        //Multi-threaded query, replacing the union all
        Map<Long, List<Long>> userIdMap = userIdNotInCacheList.stream().collect(Collectors.groupingBy(userId -> userId % 100));
        List<UserDTO> dbQueryResult = new CopyOnWriteArrayList<>();
        userIdMap.values().parallelStream().forEach(queryUserIdList -> {
            dbQueryResult.addAll(ConvertBeanUtils.convertList(userMapper.selectBatchIds(queryUserIdList), UserDTO.class));
        });

        //Put query records into redis (based on business requirements)
        if (!CollectionUtils.isEmpty(dbQueryResult)) {
            Map<String, UserDTO> saveCacheMap = dbQueryResult.stream().collect(Collectors.toMap(userDTO -> userProviderCacheKeyBuilder.buildUserInfoKey(userDTO.getUserId()), userDTO -> userDTO));
            redisTemplate.opsForValue().multiSet(saveCacheMap);
            userDTOList.addAll(dbQueryResult);
        }

        return userDTOList.stream().collect(Collectors.toMap(UserDTO::getUserId, x -> x));
    }
}
