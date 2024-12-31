package org.opengoofy.index12306.biz.orderservice.service;

import org.opengoofy.index12306.biz.orderservice.dto.domain.OrderStatusReversalDTO;
import org.opengoofy.index12306.biz.orderservice.dto.req.CancelTicketOrderReqDTO;
import org.opengoofy.index12306.biz.orderservice.dto.req.TicketOrderCreateReqDTO;
import org.opengoofy.index12306.biz.orderservice.dto.req.TicketOrderPageQueryReqDTO;
import org.opengoofy.index12306.biz.orderservice.dto.req.TicketOrderSelfPageQueryReqDTO;
import org.opengoofy.index12306.biz.orderservice.dto.resp.TicketOrderDetailRespDTO;
import org.opengoofy.index12306.biz.orderservice.dto.resp.TicketOrderDetailSelfRespDTO;
import org.opengoofy.index12306.biz.orderservice.mq.consumer.PayResultCallbackOrderConsumer;
import org.opengoofy.index12306.biz.orderservice.mq.event.PayResultCallbackOrderEvent;
import org.opengoofy.index12306.framework.starter.convention.page.PageResponse;

public interface OrderService {
    TicketOrderDetailRespDTO queryTicketOrderByOrderSn(String orderSn);

    PageResponse<TicketOrderDetailRespDTO> pageTicketOrderDetail(TicketOrderPageQueryReqDTO requestParam);

    String createTicketOrder(TicketOrderCreateReqDTO requestParam);

    boolean closeTicketOrder(CancelTicketOrderReqDTO requestParam);

    boolean cancelTicketOrder(CancelTicketOrderReqDTO requestParam);

    PageResponse<TicketOrderDetailSelfRespDTO> pageTicketOrderDetailSelf(TicketOrderSelfPageQueryReqDTO requestParam);

    void statusReversal(OrderStatusReversalDTO requestParam);

    void payCallbackOrder(PayResultCallbackOrderEvent requestParam);
}
