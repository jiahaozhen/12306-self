package org.opengoofy.index12306.biz.userservice.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengoofy.index12306.biz.userservice.dao.entity.UserDO;
import org.opengoofy.index12306.biz.userservice.dao.entity.UserDeletionDO;
import org.opengoofy.index12306.biz.userservice.dao.entity.UserMailDO;
import org.opengoofy.index12306.biz.userservice.dao.mapper.UserDeletionMapper;
import org.opengoofy.index12306.biz.userservice.dao.mapper.UserMailMapper;
import org.opengoofy.index12306.biz.userservice.dao.mapper.UserMapper;
import org.opengoofy.index12306.biz.userservice.dto.req.UserUpdateReqDTO;
import org.opengoofy.index12306.biz.userservice.dto.resp.UserQueryActualRespDTO;
import org.opengoofy.index12306.biz.userservice.dto.resp.UserQueryRespDTO;
import org.opengoofy.index12306.biz.userservice.service.UserService;
import org.opengoofy.index12306.framework.starter.common.toolkit.BeanUtil;
import org.opengoofy.index12306.framework.starter.convention.exception.ClientException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserDeletionMapper userDeletionMapper;
    private final UserMailMapper userMailMapper;

    @Override
    public UserQueryRespDTO queryUserByUserId(String userId) {
        LambdaQueryWrapper<UserDO> wrapper = Wrappers.lambdaQuery(UserDO.class).eq(UserDO::getId, userId);
        UserDO userDO = userMapper.selectOne(wrapper);
        if (userDO == null) {
            throw new ClientException("user not found, check id");
        }
        return BeanUtil.convert(userDO, UserQueryRespDTO.class);
    }

    @Override
    public UserQueryRespDTO queryUserByUsername(String userName) {
        LambdaQueryWrapper<UserDO> wrapper = Wrappers.lambdaQuery(UserDO.class).eq(UserDO::getUsername, userName);
        UserDO userDO = userMapper.selectOne(wrapper);
        if (userDO == null) {
            throw new ClientException("user not found, check username");
        }
        return BeanUtil.convert(userDO, UserQueryRespDTO.class);
    }

    @Override
    public UserQueryActualRespDTO queryActualUserByUsername(String username) {
        return BeanUtil.convert(queryUserByUsername(username), UserQueryActualRespDTO.class);
    }

    @Override
    public Integer queryUserDeletionNum(Integer idType, String idCard) {
        LambdaQueryWrapper<UserDeletionDO> queryWrapper = Wrappers.lambdaQuery(UserDeletionDO.class)
                .eq(UserDeletionDO::getIdType, idType)
                .eq(UserDeletionDO::getIdCard, idCard);
        Long del = userDeletionMapper.selectCount(queryWrapper);
        return Optional.ofNullable(del).map(Long::intValue).orElse(0);
    }

    @Override
    public void update(UserUpdateReqDTO userUpdateReqDTO) {
        UserQueryRespDTO userQueryRespDTO = queryUserByUserId(userUpdateReqDTO.getUsername());
        UserDO userDO = BeanUtil.convert(userUpdateReqDTO, UserDO.class);
        /* user mapper */
        LambdaQueryWrapper<UserDO> wrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, userUpdateReqDTO.getUsername());
        userMapper.update(userDO, wrapper);
        /* user mail */
        if (StrUtil.isNotBlank(userUpdateReqDTO.getMail()) && !Objects.equals(userUpdateReqDTO.getMail(), userQueryRespDTO.getMail())) {
            LambdaQueryWrapper<UserMailDO> updateWrapper = Wrappers.lambdaQuery(UserMailDO.class)
                    .eq(UserMailDO::getMail, userUpdateReqDTO.getMail());
            userMailMapper.delete(updateWrapper);
            UserMailDO userMailDO = UserMailDO.builder()
                    .mail(userUpdateReqDTO.getMail())
                    .username(userUpdateReqDTO.getUsername())
                    .build();
            userMailMapper.insert(userMailDO);
        }
    }
}
