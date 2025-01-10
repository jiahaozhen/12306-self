package org.opengoofy.index12306.biz.payservice.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengoofy.index12306.biz.payservice.common.enums.TradeStatusEnum;
import org.opengoofy.index12306.biz.payservice.convert.RefundRequestConvert;
import org.opengoofy.index12306.biz.payservice.dao.entity.PayDO;
import org.opengoofy.index12306.biz.payservice.dao.entity.RefundDO;
import org.opengoofy.index12306.biz.payservice.dao.mapper.PayMapper;
import org.opengoofy.index12306.biz.payservice.dao.mapper.RefundMapper;
import org.opengoofy.index12306.biz.payservice.dto.RefundCommand;
import org.opengoofy.index12306.biz.payservice.dto.RefundCreateDTO;
import org.opengoofy.index12306.biz.payservice.dto.RefundReqDTO;
import org.opengoofy.index12306.biz.payservice.dto.RefundRespDTO;
import org.opengoofy.index12306.biz.payservice.dto.base.RefundRequest;
import org.opengoofy.index12306.biz.payservice.dto.base.RefundResponse;
import org.opengoofy.index12306.biz.payservice.mq.event.RefundResultCallbackOrderEvent;
import org.opengoofy.index12306.biz.payservice.mq.produce.RefundResultCallbackOrderSendProduce;
import org.opengoofy.index12306.biz.payservice.remote.TicketOrderRemoteService;
import org.opengoofy.index12306.biz.payservice.remote.dto.TicketOrderDetailRespDTO;
import org.opengoofy.index12306.biz.payservice.service.RefundService;
import org.opengoofy.index12306.framework.starter.common.toolkit.BeanUtil;
import org.opengoofy.index12306.framework.starter.convention.exception.ServiceException;
import org.opengoofy.index12306.framework.starter.convention.result.Result;
import org.opengoofy.index12306.framework.starter.designpattern.strategy.AbstractStrategyChoose;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final PayMapper payMapper;
    private final RefundMapper refundMapper;
    private final TicketOrderRemoteService ticketOrderRemoteService;
    private final AbstractStrategyChoose abstractStrategyChoose;
    private final RefundResultCallbackOrderSendProduce refundResultCallbackOrderSendProduce;

    @Override
    public RefundRespDTO commonRefund(RefundReqDTO requestParam) {
        RefundRespDTO refundRespDTO = null;
        LambdaQueryWrapper<PayDO> queryWrapper = Wrappers.lambdaQuery(PayDO.class)
                .eq(PayDO::getOrderSn, requestParam.getOrderSn());
        PayDO payDO = payMapper.selectOne(queryWrapper);
        if (Objects.isNull(payDO)) {
            log.error("支付单不存在，orderSn：{}", requestParam.getOrderSn());
            throw new ServiceException("支付单不存在");
        }
        payDO.setPayAmount(payDO.getTotalAmount() - requestParam.getRefundAmount());
        RefundCreateDTO refundCreateDTO = BeanUtil.convert(requestParam, RefundCreateDTO.class);
        refundCreateDTO.setPaySn(payDO.getPaySn());
        createRefund(refundCreateDTO);
        RefundCommand refundCommand = BeanUtil.convert(payDO, RefundCommand.class);
        refundCommand.setPayAmount(new BigDecimal(requestParam.getRefundAmount()));
        RefundRequest refundRequest = RefundRequestConvert.command2RefundRequest(refundCommand);
        RefundResponse result = abstractStrategyChoose.chooseAndExecuteResp(refundRequest.buildMark(), refundRequest);
        payDO.setStatus(result.getStatus());
        LambdaUpdateWrapper<PayDO> updateWrapper = Wrappers.lambdaUpdate(PayDO.class)
                .eq(PayDO::getOrderSn, requestParam.getOrderSn());
        int updateResult = payMapper.update(payDO, updateWrapper);
        if (updateResult <= 0) {
            log.error("修改支付单退款结果失败，支付单信息：{}", JSON.toJSONString(payDO));
            throw new ServiceException("修改支付单退款结果失败");
        }
        LambdaUpdateWrapper<RefundDO> refundUpdateWrapper = Wrappers.lambdaUpdate(RefundDO.class)
                .eq(RefundDO::getOrderSn, requestParam.getOrderSn());
        RefundDO refundDO = new RefundDO();
        refundDO.setTradeNo(result.getTradeNo());
        refundDO.setStatus(result.getStatus());
        int refundUpdateResult = refundMapper.update(refundDO, refundUpdateWrapper);
        if (refundUpdateResult <= 0) {
            log.error("修改退款单退款结果失败，退款单信息：{}", JSON.toJSONString(refundDO));
            throw new ServiceException("修改退款单退款结果失败");
        }
        if (Objects.equals(result.getStatus(), TradeStatusEnum.TRADE_CLOSED.tradeCode())) {
            RefundResultCallbackOrderEvent refundResultCallbackOrderEvent = RefundResultCallbackOrderEvent.builder()
                    .orderSn(requestParam.getOrderSn())
                    .refundTypeEnum(requestParam.getRefundTypeEnum())
                    .partialRefundTicketDetailList(requestParam.getRefundDetailReqDTOList())
                    .build();
            refundResultCallbackOrderSendProduce.sendMessage(refundResultCallbackOrderEvent);
        }
        return refundRespDTO;
    }

    private void createRefund(RefundCreateDTO requestParam) {
        Result<TicketOrderDetailRespDTO> queryTicketResult = ticketOrderRemoteService.queryTicketOrderByOrderSn(requestParam.getOrderSn());
        if (!queryTicketResult.isSuccess() && Objects.isNull(queryTicketResult.getData())) {
            throw new ServiceException("车票订单不存在");
        }
        TicketOrderDetailRespDTO orderDetailRespDTO = queryTicketResult.getData();
        requestParam.getRefundDetailReqDTOList().forEach(each -> {
            RefundDO refundDO = new RefundDO();
            refundDO.setPaySn(requestParam.getPaySn());
            refundDO.setOrderSn(requestParam.getOrderSn());
            refundDO.setTrainId(orderDetailRespDTO.getTrainId());
            refundDO.setTrainNumber(orderDetailRespDTO.getTrainNumber());
            refundDO.setDeparture(orderDetailRespDTO.getDeparture());
            refundDO.setArrival(orderDetailRespDTO.getArrival());
            refundDO.setDepartureTime(orderDetailRespDTO.getDepartureTime());
            refundDO.setArrivalTime(orderDetailRespDTO.getArrivalTime());
            refundDO.setRidingDate(orderDetailRespDTO.getRidingDate());
            refundDO.setSeatType(each.getSeatType());
            refundDO.setIdType(each.getIdType());
            refundDO.setIdCard(each.getIdCard());
            refundDO.setRealName(each.getRealName());
            refundDO.setRefundTime(new Date());
            refundDO.setAmount(each.getAmount());
            refundDO.setUserId(each.getUserId());
            refundDO.setUsername(each.getUsername());
            refundMapper.insert(refundDO);
        });
    }
}
