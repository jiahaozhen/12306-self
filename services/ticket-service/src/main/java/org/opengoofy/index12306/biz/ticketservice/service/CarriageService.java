package org.opengoofy.index12306.biz.ticketservice.service;

import java.util.List;

public interface CarriageService {

    List<String> listCarriageNumber(String trainId, Integer carriageType);
}
