package org.opengoofy.index12306.biz.ticketservice.service;

import org.opengoofy.index12306.biz.ticketservice.dto.req.RegionStationQueryReqDTO;
import org.opengoofy.index12306.biz.ticketservice.dto.resp.RegionStationQueryRespDTO;
import org.opengoofy.index12306.biz.ticketservice.dto.resp.StationQueryRespDTO;

import java.util.List;

public interface RegionStationService {

    List<RegionStationQueryRespDTO> listRegionStations(RegionStationQueryReqDTO requestParam);

    List<StationQueryRespDTO> listAllStation();
}
