package org.opengoofy.index12306.biz.payservice.service;

import org.opengoofy.index12306.biz.payservice.dto.PayCallbackReqDTO;
import org.opengoofy.index12306.biz.payservice.dto.PayInfoRespDTO;
import org.opengoofy.index12306.biz.payservice.dto.PayRespDTO;
import org.opengoofy.index12306.biz.payservice.dto.base.PayRequest;

public interface PayService {

    PayRespDTO commonPay(PayRequest requestParam);

    PayInfoRespDTO getPayInfoByOrderSn(String orderSn);

    PayInfoRespDTO getPayInfoByPaySn(String paySn);

    void callbackPay(PayCallbackReqDTO requestParam);
}
