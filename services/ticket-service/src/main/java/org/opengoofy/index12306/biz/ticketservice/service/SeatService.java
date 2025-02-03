package org.opengoofy.index12306.biz.ticketservice.service;

import org.opengoofy.index12306.biz.ticketservice.dto.domain.SeatTypeCountDTO;
import org.opengoofy.index12306.biz.ticketservice.service.handler.ticket.dto.TrainPurchaseTicketRespDTO;

import java.util.List;

public interface SeatService {

    List<String> listAvailableSeat(String trainId, String carriageNumber, Integer seatType, String departure, String arrival);

    List<Integer> listSeatRemainingTicket(String trainId, String departure, String arrival, List<String> trainCarriageList);

    List<String> listUsableCarriageNumber(String trainId, Integer carriageType, String departure, String arrival);

    List<SeatTypeCountDTO> listSeatTypeCount(Long trainId, String startStation, String endStation, List<Integer> seatTypes);

    void lockSeat(String trainId, String departure, String arrival, List<TrainPurchaseTicketRespDTO> trainPurchaseTicketRespList);

    void unlock(String trainId, String departure, String arrival, List<TrainPurchaseTicketRespDTO> trainPurchaseTicketResults);

}
