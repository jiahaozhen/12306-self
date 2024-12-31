package org.opengoofy.index12306.biz.orderservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.opengoofy.index12306.biz.orderservice.dao.entity.OrderItemPassengerDO;
import org.opengoofy.index12306.biz.orderservice.dao.mapper.OrderItemPassengerMapper;
import org.opengoofy.index12306.biz.orderservice.service.OrderPassengerRelationService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderPassengerRelationServiceImpl extends ServiceImpl<OrderItemPassengerMapper, OrderItemPassengerDO> implements OrderPassengerRelationService {
}
