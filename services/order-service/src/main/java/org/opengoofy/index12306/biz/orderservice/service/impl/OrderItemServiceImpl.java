package org.opengoofy.index12306.biz.orderservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengoofy.index12306.biz.orderservice.dao.entity.OrderItemDO;
import org.opengoofy.index12306.biz.orderservice.dao.mapper.OrderItemMapper;
import org.opengoofy.index12306.biz.orderservice.dto.domain.OrderItemStatusReversalDTO;
import org.opengoofy.index12306.biz.orderservice.dto.req.TicketOrderItemQueryReqDTO;
import org.opengoofy.index12306.biz.orderservice.dto.resp.TicketOrderPassengerDetailRespDTO;
import org.opengoofy.index12306.biz.orderservice.service.OrderItemService;
import org.opengoofy.index12306.framework.starter.common.toolkit.BeanUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItemDO> implements OrderItemService {
    private final OrderItemMapper orderItemMapper;

    @Override
    public void orderItemStatusReversal(OrderItemStatusReversalDTO requestPara) {

    }

    @Override
    public List<TicketOrderPassengerDetailRespDTO> queryTicketItemOrderById(TicketOrderItemQueryReqDTO requestParam) {
        LambdaQueryWrapper<OrderItemDO> queryWrapper = Wrappers.lambdaQuery(OrderItemDO.class)
                .eq(OrderItemDO::getOrderSn, requestParam.getOrderSn())
                .in(OrderItemDO::getId, requestParam.getOrderItemRecordIds());
        List<OrderItemDO> orderItemDOList = orderItemMapper.selectList(queryWrapper);
        return BeanUtil.convert(orderItemDOList, TicketOrderPassengerDetailRespDTO.class);
    }
}
