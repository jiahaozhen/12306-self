package org.opengoofy.index12306.biz.payservice.service;

import org.opengoofy.index12306.biz.payservice.dto.RefundReqDTO;
import org.opengoofy.index12306.biz.payservice.dto.RefundRespDTO;

public interface RefundService {

    RefundRespDTO commonRefund(RefundReqDTO requestParam);

}
