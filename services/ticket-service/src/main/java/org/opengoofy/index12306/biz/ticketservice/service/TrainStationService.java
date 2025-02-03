package org.opengoofy.index12306.biz.ticketservice.service;

import org.opengoofy.index12306.biz.ticketservice.dto.domain.RouteDTO;
import org.opengoofy.index12306.biz.ticketservice.dto.resp.TrainStationQueryRespDTO;

import java.util.List;

public interface TrainStationService {

    List<TrainStationQueryRespDTO> listTrainStationQuery(String trainId);

    List<RouteDTO> listTrainStationRoute(String trainId, String departure, String arrival);

    List<RouteDTO> listTakeoutTrainStationRoute(String trainId, String departure, String arrival);
}
