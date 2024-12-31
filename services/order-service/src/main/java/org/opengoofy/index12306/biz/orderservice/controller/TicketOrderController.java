package org.opengoofy.index12306.biz.orderservice.controller;

import cn.crane4j.annotation.AutoOperate;
import cn.hutool.db.PageResult;
import lombok.RequiredArgsConstructor;
import org.opengoofy.index12306.biz.orderservice.dto.domain.OrderItemStatusReversalDTO;
import org.opengoofy.index12306.biz.orderservice.dto.req.*;
import org.opengoofy.index12306.biz.orderservice.dto.resp.TicketOrderDetailRespDTO;
import org.opengoofy.index12306.biz.orderservice.dto.resp.TicketOrderDetailSelfRespDTO;
import org.opengoofy.index12306.biz.orderservice.dto.resp.TicketOrderPassengerDetailRespDTO;
import org.opengoofy.index12306.biz.orderservice.service.OrderItemService;
import org.opengoofy.index12306.biz.orderservice.service.OrderService;
import org.opengoofy.index12306.framework.starter.convention.page.PageResponse;
import org.opengoofy.index12306.framework.starter.convention.result.Result;
import org.opengoofy.index12306.framework.starter.web.Results;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TicketOrderController {

    private final OrderService orderService;
    private final OrderItemService orderItemService;

    @GetMapping("/api/order-service/order/ticket/query")
    public Result<TicketOrderDetailRespDTO> queryTicketOrderByOrderSn(@RequestParam(value = "orderSn") String orderSn) {
        return Results.success(orderService.queryTicketOrderByOrderSn(orderSn));
    }

    @AutoOperate(type = TicketOrderDetailRespDTO.class, on = "data.records")
    @GetMapping("/api/order-service/order/ticket/page")
    public Result<PageResponse<TicketOrderDetailRespDTO>> pageTicketOrder(TicketOrderPageQueryReqDTO requestParam) {
        return Results.success(orderService.pageTicketOrderDetail(requestParam));
    }

    @GetMapping("/api/order-service/order/ticket/self/page")
    public Result<PageResponse<TicketOrderDetailSelfRespDTO>> pageSelfTicketOrder(TicketOrderSelfPageQueryReqDTO requestParam) {
        return Results.success(orderService.pageTicketOrderDetailSelf(requestParam));
    }

    @PostMapping("/api/order-service/order/ticket/create")
    public Result<String> createTicketOrder(@RequestBody TicketOrderCreateReqDTO requestParam) {
        return Results.success(orderService.createTicketOrder(requestParam));
    }

    @PostMapping("/api/order-service/order/ticket/close")
    public Result<Boolean> closeTicketOrder(@RequestBody CancelTicketOrderReqDTO requestParam) {
        return Results.success(orderService.closeTicketOrder(requestParam));
    }

    @PostMapping("/api/order-service/order/ticket/cancel")
    public Result<Boolean> cancelTicketOrder(@RequestBody CancelTicketOrderReqDTO requestParam) {
        return Results.success(orderService.cancelTicketOrder(requestParam));
    }

    @GetMapping("/api/order-service/order/item/ticket/query")
    public Result<List<TicketOrderPassengerDetailRespDTO>> queryTicketItemOrderById(TicketOrderItemQueryReqDTO requestParam) {
        return Results.success(orderItemService.queryTicketItemOrderById(requestParam));
    }
}
