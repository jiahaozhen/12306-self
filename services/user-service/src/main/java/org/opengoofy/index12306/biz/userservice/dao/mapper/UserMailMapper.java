package org.opengoofy.index12306.biz.userservice.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.opengoofy.index12306.biz.userservice.dao.entity.UserMailDO;

public interface UserMailMapper extends BaseMapper<UserMailDO> {
    void deletionUser(UserMailDO userMailDO);
}
