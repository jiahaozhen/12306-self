package org.opengoofy.index12306.biz.ticketservice.service;

import org.opengoofy.index12306.biz.ticketservice.dto.req.CancelTicketOrderReqDTO;
import org.opengoofy.index12306.biz.ticketservice.dto.req.PurchaseTicketReqDTO;
import org.opengoofy.index12306.biz.ticketservice.dto.req.RefundTicketReqDTO;
import org.opengoofy.index12306.biz.ticketservice.dto.req.TicketPageQueryReqDTO;
import org.opengoofy.index12306.biz.ticketservice.dto.resp.RefundTicketRespDTO;
import org.opengoofy.index12306.biz.ticketservice.dto.resp.TicketPageQueryRespDTO;
import org.opengoofy.index12306.biz.ticketservice.dto.resp.TicketPurchaseRespDTO;
import org.opengoofy.index12306.biz.ticketservice.remote.dto.PayInfoRespDTO;
import org.springframework.web.bind.annotation.RequestBody;

public interface TicketService {

    TicketPageQueryRespDTO pageListTicketQuery(TicketPageQueryReqDTO requestParam);

    TicketPurchaseRespDTO purchaseTickets(@RequestBody PurchaseTicketReqDTO requestParam);

    TicketPurchaseRespDTO executePurchaseTickets(@RequestBody PurchaseTicketReqDTO requestParam);

    PayInfoRespDTO getPayInfo(String orderSn);

    void cancelTicketOrder(CancelTicketOrderReqDTO requestParam);

    RefundTicketRespDTO commonTicketRefund(RefundTicketReqDTO requestParam);
}
