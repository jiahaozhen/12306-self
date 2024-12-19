package org.opengoofy.index12306.biz.gatewayservice.filter;

import org.opengoofy.index12306.biz.gatewayservice.config.Config;
import org.opengoofy.index12306.biz.gatewayservice.toolkit.JWTUtil;
import org.opengoofy.index12306.biz.gatewayservice.toolkit.UserInfoDTO;
import org.opengoofy.index12306.framework.starter.bases.constant.UserConstant;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Component
public class TokeValidateGatewayFilterFactory extends AbstractGatewayFilterFactory<Config> {

    public TokeValidateGatewayFilterFactory() {super(Config.class);}

    public static final String DELETION_PATH = "/api/user-service/deletion";

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String requestPath = request.getPath().toString();
            if (isPathInBlackPreList(requestPath, config.getBlackPathPre())) {
                String token = request.getHeaders().getFirst("Authorization");
                UserInfoDTO userInfoDTO = JWTUtil.parseJwtToken(token);
                if (!validateToken(userInfoDTO)) {
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return response.setComplete();
                }
                ServerHttpRequest.Builder builder = exchange.getRequest().mutate().headers(httpHeaders -> {
                    httpHeaders.set(UserConstant.USER_ID_KEY, userInfoDTO.getUserId());
                    httpHeaders.set(UserConstant.USER_NAME_KEY, userInfoDTO.getUsername());
                    httpHeaders.set(UserConstant.REAL_NAME_KEY, userInfoDTO.getRealName());
                    if (Objects.equals(requestPath, DELETION_PATH)) {
                        httpHeaders.set(UserConstant.USER_TOKEN_KEY, token);
                    }
                });
                return chain.filter(exchange.mutate().request(builder.build()).build());
            }
            return chain.filter(exchange);
        };
    }

    private boolean isPathInBlackPreList(String requestPath, List<String> blackPathPre) {
        if (CollectionUtils.isEmpty(blackPathPre)) {
            return false;
        }
        return blackPathPre.stream().anyMatch(requestPath::startsWith);
    }

    private boolean validateToken(UserInfoDTO userInfo) {
        return userInfo != null;
    }
}
