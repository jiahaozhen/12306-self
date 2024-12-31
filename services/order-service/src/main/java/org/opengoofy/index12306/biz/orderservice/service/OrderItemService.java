package org.opengoofy.index12306.biz.orderservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengoofy.index12306.biz.orderservice.dao.entity.OrderItemDO;
import org.opengoofy.index12306.biz.orderservice.dto.domain.OrderItemStatusReversalDTO;
import org.opengoofy.index12306.biz.orderservice.dto.req.TicketOrderItemQueryReqDTO;
import org.opengoofy.index12306.biz.orderservice.dto.resp.TicketOrderPassengerDetailRespDTO;

import java.util.List;

public interface OrderItemService extends IService<OrderItemDO> {

    void orderItemStatusReversal(OrderItemStatusReversalDTO requestPara);

    List<TicketOrderPassengerDetailRespDTO> queryTicketItemOrderById(TicketOrderItemQueryReqDTO requestParam);
}
