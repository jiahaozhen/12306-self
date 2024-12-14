package org.opengoofy.index12306.biz.userservice.service;

import org.opengoofy.index12306.biz.userservice.dto.req.UserUpdateReqDTO;
import org.opengoofy.index12306.biz.userservice.dto.resp.UserQueryActualRespDTO;
import org.opengoofy.index12306.biz.userservice.dto.resp.UserQueryRespDTO;

public interface UserService {
    UserQueryRespDTO queryUserByUserId(String userId);

    UserQueryRespDTO queryUserByUsername(String userName);

    UserQueryActualRespDTO queryActualUserByUsername(String username);

    Integer queryUserDeletionNum(Integer idType, String idCard);

    void update(UserUpdateReqDTO userUpdateReqDTO);
}
