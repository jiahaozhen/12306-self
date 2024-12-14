package org.opengoofy.index12306.biz.userservice.service;

import org.opengoofy.index12306.biz.userservice.dto.req.PassengerRemoveReqDTO;
import org.opengoofy.index12306.biz.userservice.dto.req.PassengerReqDTO;
import org.opengoofy.index12306.biz.userservice.dto.resp.PassengerActualRespDTO;
import org.opengoofy.index12306.biz.userservice.dto.resp.PassengerRespDTO;

import java.util.List;

public interface PassengerService {
    List<PassengerRespDTO> listPassengerQueryByUsername(String username);

    List<PassengerActualRespDTO> listPassengerQueryById(String username, List<Long> ids);

    void savePassenger(PassengerReqDTO passenger);

    void updatePassenger(PassengerReqDTO passenger);

    void removePassenger(PassengerRemoveReqDTO passenger);
}
