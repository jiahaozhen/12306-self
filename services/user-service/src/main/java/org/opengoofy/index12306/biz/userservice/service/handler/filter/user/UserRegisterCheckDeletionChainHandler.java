package org.opengoofy.index12306.biz.userservice.service.handler.filter.user;

import lombok.RequiredArgsConstructor;
import org.opengoofy.index12306.biz.userservice.dto.req.UserRegisterReqDTO;
import org.opengoofy.index12306.biz.userservice.service.UserService;
import org.opengoofy.index12306.framework.starter.convention.exception.ClientException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRegisterCheckDeletionChainHandler implements UserRegisterCreateChainFilter<UserRegisterReqDTO>{

    private final UserService userService;

    @Override
    public void handler(UserRegisterReqDTO requestParam) {
        Integer deletionNum = userService.queryUserDeletionNum(requestParam.getIdType(), requestParam.getIdCard());
        if (deletionNum >= 5) {
            throw new ClientException("deletion too many times.");
        }
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
