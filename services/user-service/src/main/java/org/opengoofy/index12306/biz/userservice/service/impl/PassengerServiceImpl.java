package org.opengoofy.index12306.biz.userservice.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengoofy.index12306.biz.userservice.common.enums.VerifyStatusEnum;
import org.opengoofy.index12306.biz.userservice.dao.entity.PassengerDO;
import org.opengoofy.index12306.biz.userservice.dao.mapper.PassengerMapper;
import org.opengoofy.index12306.biz.userservice.dto.req.PassengerRemoveReqDTO;
import org.opengoofy.index12306.biz.userservice.dto.req.PassengerReqDTO;
import org.opengoofy.index12306.biz.userservice.dto.resp.PassengerActualRespDTO;
import org.opengoofy.index12306.biz.userservice.dto.resp.PassengerRespDTO;
import org.opengoofy.index12306.biz.userservice.service.PassengerService;
import org.opengoofy.index12306.framework.starter.cache.DistributedCache;
import org.opengoofy.index12306.framework.starter.common.toolkit.BeanUtil;
import org.opengoofy.index12306.framework.starter.convention.exception.ClientException;
import org.opengoofy.index12306.framework.starter.convention.exception.ServiceException;
import org.opengoofy.index12306.frameworks.starter.user.core.UserContext;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.opengoofy.index12306.biz.userservice.common.constant.RedisKeyConstant.USER_PASSENGER_LIST;

@Slf4j
@Service
@RequiredArgsConstructor
public class PassengerServiceImpl implements PassengerService {
    private final DistributedCache distributedCache;
    private final PassengerMapper passengerMapper;

    @Override
    public List<PassengerRespDTO> listPassengerQueryByUsername(String username) {
        String actualUserPassengerListStr = getActualUserPassengerListStr(username);
        return Optional.ofNullable(actualUserPassengerListStr)
                .map(each -> JSON.parseArray(each, PassengerDO.class))
                .map(each -> BeanUtil.convert(each, PassengerRespDTO.class))
                .orElse(null);
    }

    @Override
    public List<PassengerActualRespDTO> listPassengerQueryById(String username, List<Long> ids) {
        String actualUserPassengerListStr = getActualUserPassengerListStr(username);
        if (StrUtil.isEmpty(actualUserPassengerListStr)) {
            return null;
        }
        return JSON.parseArray(actualUserPassengerListStr, PassengerDO.class)
                .stream().filter(passengerDO -> ids.contains(passengerDO.getId()))
                .map(each -> BeanUtil.convert(each, PassengerActualRespDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void savePassenger(PassengerReqDTO passenger) {
        vertifyPassenger(passenger);
        String username = UserContext.getUsername();
        try {
            PassengerDO passengerDO = BeanUtil.convert(passenger, PassengerDO.class);
            passengerDO.setUsername(username);
            passengerDO.setCreateDate(new Date());
            passengerDO.setVerifyStatus(VerifyStatusEnum.REVIEWED.getCode());
            int insert = passengerMapper.insert(passengerDO);
            if (!SqlHelper.retBool(insert)) {
                throw new ServiceException(String.format("[%s] add passenger failed", username));
            }
        } catch (Exception ex) {
            if (ex instanceof ServiceException) {
                log.error("{}, parameter:, {}", ex.getMessage(), JSON.toJSONString(passenger));
            } else {
                log.error("[{}] add new passenger fail, parameter: {}", username, JSON.toJSONString(passenger), ex);
            }
            throw ex;
        }
        delUsePassengerCache(username);
    }

    @Override
    public void updatePassenger(PassengerReqDTO passenger) {
        vertifyPassenger(passenger);
        String username = UserContext.getUsername();
        try {
            PassengerDO passengerDO = BeanUtil.convert(passenger, PassengerDO.class);
            passengerDO.setUsername(username);
            LambdaUpdateWrapper<PassengerDO> updateWrapper = Wrappers.lambdaUpdate(PassengerDO.class)
                    .eq(PassengerDO::getUsername, username)
                    .eq(PassengerDO::getId, passenger.getId());
            int updated = passengerMapper.update(passengerDO, updateWrapper);
            if (!SqlHelper.retBool(updated)) {
                throw new ServiceException(String.format("[%s] update passenger fail", username));
            }
        } catch (Exception ex) {
            if (ex instanceof ServiceException) {
                log.error("{}，parameter: {}", ex.getMessage(), JSON.toJSONString(passenger));
            } else {
                log.error("[{}] update passenger fail, parameter: {}", username, JSON.toJSONString(passenger), ex);
            }
            throw ex;
        }
        delUsePassengerCache(username);
    }

    @Override
    public void removePassenger(PassengerRemoveReqDTO passenger) {
        String username = UserContext.getUsername();
        PassengerDO passengerDO = selectPassenger(username, passenger.getId());
        if (Objects.isNull(passengerDO)) {
            throw new ClientException("no such passenger");
        }
        try {
            LambdaUpdateWrapper<PassengerDO> deleteWrapper = Wrappers.lambdaUpdate(PassengerDO.class)
                    .eq(PassengerDO::getUsername, username)
                    .eq(PassengerDO::getId, passenger.getId());
            int deleted = passengerMapper.delete(deleteWrapper);
            if (!SqlHelper.retBool(deleted)) {
                throw new ServiceException(String.format("[%s] delete passenger fail", username));
            }
        } catch (Exception ex) {
            if (ex instanceof ServiceException) {
                log.error("{}，parameter: {}", ex.getMessage(), JSON.toJSONString(passenger));
            } else {
                log.error("[{}] delete passenger fail, parameter: {}", username, JSON.toJSONString(passenger), ex);
            }
            throw ex;
        }
        delUsePassengerCache(username);
    }

    private String getActualUserPassengerListStr(String username) {
        return distributedCache.safeGet(
                USER_PASSENGER_LIST + username,
                String.class,
                () -> {
                    LambdaQueryWrapper<PassengerDO> queryWrapper = Wrappers.lambdaQuery(PassengerDO.class)
                            .eq(PassengerDO::getUsername, username);
                    List<PassengerDO> passengerDOList = passengerMapper.selectList(queryWrapper);
                    return CollUtil.isNotEmpty(passengerDOList) ? com.alibaba.fastjson2.JSON.toJSONString(passengerDOList) : null;
                },
                1,
                TimeUnit.DAYS
            );
    }

    private PassengerDO selectPassenger(String username, String passengerId) {
        LambdaQueryWrapper<PassengerDO> queryWrapper = Wrappers.lambdaQuery(PassengerDO.class)
                .eq(PassengerDO::getUsername, username)
                .eq(PassengerDO::getId, passengerId);
        return passengerMapper.selectOne(queryWrapper);
    }

    private void delUsePassengerCache(String username) {
        distributedCache.delete(USER_PASSENGER_LIST + username);
    }

    private void vertifyPassenger(PassengerReqDTO passenger) {
        int length = passenger.getRealName().length();
        if (!(length >= 2 && length <= 16)) {
            throw new ClientException("name to long");
        }
        if (!IdcardUtil.isValidCard(passenger.getIdCard())) {
            throw new ClientException("id-card invalid");
        }
        if (!PhoneUtil.isMobile(passenger.getPhone())) {
            throw new ClientException("phone number invalid");
        }
    }
}
