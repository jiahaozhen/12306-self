package org.opengoofy.index12306.biz.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.opengoofy.index12306.biz.userservice.dto.req.UserDeletionReqDTO;
import org.opengoofy.index12306.biz.userservice.dto.req.UserRegisterReqDTO;
import org.opengoofy.index12306.biz.userservice.dto.req.UserUpdateReqDTO;
import org.opengoofy.index12306.biz.userservice.dto.resp.UserQueryActualRespDTO;
import org.opengoofy.index12306.biz.userservice.dto.resp.UserQueryRespDTO;
import org.opengoofy.index12306.biz.userservice.dto.resp.UserRegisterRespDTO;
import org.opengoofy.index12306.biz.userservice.service.UserLoginService;
import org.opengoofy.index12306.biz.userservice.service.UserService;
import org.opengoofy.index12306.framework.starter.convention.result.Result;
import org.opengoofy.index12306.framework.starter.web.Results;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserInfoController {

    private final UserLoginService userLoginService;
    private final UserService userService;

    @GetMapping("/api/user-service/query")
    public Result<UserQueryRespDTO> queryUserByUsername(@RequestParam("username") String username) {
        return Results.success(userService.queryUserByUsername(username));
    }

    @GetMapping("/api/user-service/actual/query")
    public Result<UserQueryActualRespDTO> queryActualUserByUsername(@RequestParam("username") String username) {
        return Results.success(userService.queryActualUserByUsername(username));
    }

    @GetMapping("/api/user-service/has-username")
    public Result<Boolean> hasUsername(@RequestParam("username") String username) {
        return Results.success(userLoginService.hasUsername(username));
    }

    @PostMapping("/api/user-service/register")
    public Result<UserRegisterRespDTO> register(@RequestBody UserRegisterReqDTO userRegisterReqDTO) {
        return Results.success(userLoginService.register(userRegisterReqDTO));
    }

    @PostMapping("/api/user-service/update")
    public Result<Void> update(@RequestBody UserUpdateReqDTO userUpdateReqDTO) {
        userService.update(userUpdateReqDTO);
        return Results.success();
    }

    @PostMapping("/api/user-service/deletion")
    public Result<Void> deletion(@RequestBody UserDeletionReqDTO userDeletionReqDTO) {
        userLoginService.deletion(userDeletionReqDTO);
        return Results.success();
    }
}
