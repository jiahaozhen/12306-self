package org.opengoofy.index12306.biz.userservice.service;

import org.opengoofy.index12306.biz.userservice.dto.req.UserDeletionReqDTO;
import org.opengoofy.index12306.biz.userservice.dto.req.UserLoginReqDTO;
import org.opengoofy.index12306.biz.userservice.dto.req.UserRegisterReqDTO;
import org.opengoofy.index12306.biz.userservice.dto.resp.UserLoginRespDTO;
import org.opengoofy.index12306.biz.userservice.dto.resp.UserRegisterRespDTO;

public interface UserLoginService {

    UserLoginRespDTO login(UserLoginReqDTO userLoginReqDTO);

    UserLoginRespDTO checkLogin(String accessToken);

    void logout(String accessToken);

    Boolean hasUsername(String username);

    UserRegisterRespDTO register(UserRegisterReqDTO userRegisterReqDTO);

    void deletion(UserDeletionReqDTO userDeletionReqDTO);
}
