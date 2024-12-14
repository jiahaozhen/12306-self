package org.opengoofy.index12306.biz.userservice.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.opengoofy.index12306.biz.userservice.dao.entity.UserPhoneDO;

public interface UserPhoneMapper extends BaseMapper<UserPhoneDO> {
    void deletionUser(UserPhoneDO userPhoneDO);
}
