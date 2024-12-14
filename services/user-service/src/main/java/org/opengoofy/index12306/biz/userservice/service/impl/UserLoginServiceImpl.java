package org.opengoofy.index12306.biz.userservice.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengoofy.index12306.biz.userservice.common.enums.UserChainMarkEnum;
import org.opengoofy.index12306.biz.userservice.dao.entity.*;
import org.opengoofy.index12306.biz.userservice.dao.mapper.*;
import org.opengoofy.index12306.biz.userservice.dto.req.UserDeletionReqDTO;
import org.opengoofy.index12306.biz.userservice.dto.req.UserLoginReqDTO;
import org.opengoofy.index12306.biz.userservice.dto.req.UserRegisterReqDTO;
import org.opengoofy.index12306.biz.userservice.dto.resp.UserLoginRespDTO;
import org.opengoofy.index12306.biz.userservice.dto.resp.UserQueryRespDTO;
import org.opengoofy.index12306.biz.userservice.dto.resp.UserRegisterRespDTO;
import org.opengoofy.index12306.biz.userservice.service.UserLoginService;
import org.opengoofy.index12306.biz.userservice.service.UserService;
import org.opengoofy.index12306.framework.starter.cache.DistributedCache;
import org.opengoofy.index12306.framework.starter.common.toolkit.BeanUtil;
import org.opengoofy.index12306.framework.starter.convention.exception.ClientException;
import org.opengoofy.index12306.framework.starter.convention.exception.ServiceException;
import org.opengoofy.index12306.framework.starter.designpattern.chain.AbstractChainContext;
import org.opengoofy.index12306.frameworks.starter.user.core.UserContext;
import org.opengoofy.index12306.frameworks.starter.user.core.UserInfoDTO;
import org.opengoofy.index12306.frameworks.starter.user.toolkit.JWTUtil;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.opengoofy.index12306.biz.userservice.common.constant.RedisKeyConstant.*;
import static org.opengoofy.index12306.biz.userservice.common.enums.UserRegisterErrorCodeEnum.*;
import static org.opengoofy.index12306.biz.userservice.toolkit.UserReuseUtil.hashShardingIdx;


@RequiredArgsConstructor
@Slf4j
@Service
public class UserLoginServiceImpl implements UserLoginService {

    private final DistributedCache distributedCache;
    private final UserService userService;
    private final UserMailMapper userMailMapper;
    private final UserPhoneMapper userPhoneMapper;
    private final UserMapper userMapper;
    private final RedissonClient redissonClient;
    private final AbstractChainContext<UserRegisterReqDTO> abstractChainContext;
    private final UserReuseMapper userReuseMapper;
    /* contain used username */
    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final UserDeletionMapper userDeletionMapper;


    @Override
    public UserLoginRespDTO login(UserLoginReqDTO userLoginReqDTO) {
        String usernameOrMailOrPhone = userLoginReqDTO.getUsernameOrMailOrPhone();
        /* email? */
        boolean mailFlag = false;
        for (char c : usernameOrMailOrPhone.toCharArray()) {
            if (c == '@') {
                mailFlag = true;
                break;
            }
        }
        String username;
        if (mailFlag) {
            LambdaQueryWrapper<UserMailDO> eq = Wrappers.lambdaQuery(UserMailDO.class).eq(UserMailDO::getMail, usernameOrMailOrPhone);
            username = Optional.ofNullable(userMailMapper.selectOne(eq))
                    .map(UserMailDO::getUsername)
                    .orElseThrow(() -> new ClientException("username or mail does not exist"));
        } else {
            LambdaQueryWrapper<UserPhoneDO> eq = Wrappers.lambdaQuery(UserPhoneDO.class).eq(UserPhoneDO::getPhone, usernameOrMailOrPhone);
            username = Optional.ofNullable(userPhoneMapper.selectOne(eq))
                    .map(UserPhoneDO::getUsername)
                    .orElse(null);
        }
        username = Optional.ofNullable(username).orElse(userLoginReqDTO.getUsernameOrMailOrPhone());
        /* now we know the username */
        LambdaQueryWrapper<UserDO> select = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username)
                .eq(UserDO::getPassword, userLoginReqDTO.getPassword())
                .select(UserDO::getId, UserDO::getUsername, UserDO::getRealName);
        UserDO userDO = userMapper.selectOne(select);
        if (userDO != null) {
            UserInfoDTO userInfo = UserInfoDTO.builder()
                    .userId(String.valueOf(userDO.getId()))
                    .username(userDO.getUsername())
                    .realName(userDO.getRealName())
                    .build();
            String accessToken = JWTUtil.generateAccessToken(userInfo);
            UserLoginRespDTO userLoginRespDTO = new UserLoginRespDTO(userInfo.getUserId(), userLoginReqDTO.getUsernameOrMailOrPhone(), userDO.getRealName(), accessToken);
            distributedCache.put(accessToken, JSON.toJSONString(userLoginRespDTO), 30, TimeUnit.MINUTES);
            return userLoginRespDTO;
        }
        throw new ServiceException("wrong username or password");
    }

    @Override
    public UserLoginRespDTO checkLogin(String accessToken) {
        return distributedCache.get(accessToken, UserLoginRespDTO.class);
    }

    @Override
    public void logout(String accessToken) {
        if (StrUtil.isNotBlank(accessToken)) {
            distributedCache.delete(accessToken);
        }
    }

    @Override
    public Boolean hasUsername(String username) {
        /* true means username is used*/
        boolean hasUsername = userRegisterCachePenetrationBloomFilter.contains(username);
        if (hasUsername) {
            StringRedisTemplate instance = (StringRedisTemplate) distributedCache.getInstance();
            /* true means username is not used */
            return instance.opsForSet().isMember(USER_REGISTER_REUSE_SHARDING + hashShardingIdx(username), username);
        }
        /* username is not used */
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserRegisterRespDTO register(UserRegisterReqDTO userRegisterReqDTO) {
        abstractChainContext.handler(UserChainMarkEnum.USER_REGISTER_FILTER.name(), userRegisterReqDTO);
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER + userRegisterReqDTO.getUsername());
        boolean tryLock = lock.tryLock();
        if (!tryLock) {
            throw new ServiceException(HAS_USERNAME_NOTNULL);
        }
        try {
            try {
                int insert = userMapper.insert(BeanUtil.convert(userRegisterReqDTO, UserDO.class));
                if (insert < 1) {
                    throw new ServiceException(USER_REGISTER_FAIL);
                }
            } catch (DuplicateKeyException e) {
                log.error("username [{}] exist", userRegisterReqDTO.getUsername());
                throw new ServiceException(HAS_USERNAME_NOTNULL);
            }
            UserPhoneDO userPhoneDO = UserPhoneDO.builder()
                    .phone(userRegisterReqDTO.getPhone())
                    .username(userRegisterReqDTO.getUsername())
                    .build();
            try {
                userPhoneMapper.insert(userPhoneDO);
            } catch (DuplicateKeyException e) {
                log.error("phone number [{}] exist", userPhoneDO.getPhone());
                throw new ServiceException(PHONE_REGISTERED);
            }
            if (StrUtil.isNotBlank(userRegisterReqDTO.getMail())) {
                UserMailDO userMailDO = UserMailDO.builder()
                        .mail(userRegisterReqDTO.getMail())
                        .username(userRegisterReqDTO.getUsername())
                        .build();
                try {
                    userMailMapper.insert(userMailDO);
                } catch (DuplicateKeyException e) {
                    log.error("mail number [{}] exist", userMailDO.getMail());
                    throw new ServiceException(MAIL_REGISTERED);
                }
            }
            String username = userRegisterReqDTO.getUsername();
            userReuseMapper.delete(Wrappers.update(new UserReuseDO(username)));
            StringRedisTemplate instance = (StringRedisTemplate) distributedCache.getInstance();
            instance.opsForSet().remove(USER_REGISTER_REUSE_SHARDING + hashShardingIdx(username), username);
            userRegisterCachePenetrationBloomFilter.add(username);
        } finally {
            lock.unlock();
        }
        return BeanUtil.convert(userRegisterReqDTO, UserRegisterRespDTO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deletion(UserDeletionReqDTO userDeletionReqDTO) {
        /* username not match*/
        String username = UserContext.getUsername();
        if (!Objects.equals(userDeletionReqDTO.getUsername(), username)) {
            throw new ClientException("username [" + username + "] does not match");
        }
        RLock lock = redissonClient.getLock(USER_DELETION + userDeletionReqDTO.getUsername());
        lock.lock();
        try {
            /* deletion map */
            UserQueryRespDTO userQueryRespDTO = userService.queryUserByUsername(userDeletionReqDTO.getUsername());
            UserDeletionDO userDeletionDO = UserDeletionDO.builder()
                    .idType(userQueryRespDTO.getIdType())
                    .idCard(userQueryRespDTO.getIdCard())
                    .build();
            userDeletionMapper.insert(userDeletionDO);
            /* user delete */
            UserDO userDO = new UserDO();
            userDO.setDeletionTime(System.currentTimeMillis());
            userDO.setUsername(userDeletionReqDTO.getUsername());
            userMapper.deletionUser(userDO);
            /* user phone delete */
            UserPhoneDO userPhoneDO = UserPhoneDO.builder()
                    .phone(userQueryRespDTO.getPhone())
                    .deletionTime(System.currentTimeMillis())
                    .build();
            userPhoneMapper.deletionUser(userPhoneDO);
            /* user mail delete */
            if (StrUtil.isNotBlank(userQueryRespDTO.getMail())) {
                UserMailDO userMailDO = UserMailDO.builder()
                        .mail(userQueryRespDTO.getMail())
                        .deletionTime(System.currentTimeMillis())
                        .build();
                userMailMapper.deletionUser(userMailDO);
            }
            /* delete in cache */
            distributedCache.delete(UserContext.getToken());
            /* reuse username */
            userReuseMapper.insert(new UserReuseDO(username));
            StringRedisTemplate instance = (StringRedisTemplate)distributedCache.getInstance();;
            instance.opsForSet().add(USER_REGISTER_REUSE_SHARDING + hashShardingIdx(username), username);
        } finally {
            lock.unlock();
        }
    }
}
