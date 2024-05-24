package com.cmsr.hik.vision.controller;

import com.cmsr.hik.vision.model.parking.ParkingHisRecord;
import com.cmsr.hik.vision.model.parking.Resp;
import com.cmsr.hik.vision.service.HikVisionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HikVisionController {
    @Autowired
    private HikVisionService hikVisionService;

    @GetMapping(value = "/hikvision/parking/info/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getParkingInfo() {
        hikVisionService.updateParkingInfo(1, 10);

        return null;
    }

    @GetMapping(value = "/hikvision/parking/record/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public Resp<ParkingHisRecord> getParkingRecord() {
        hikVisionService.updateParkingRecord(1, 10);

        return null;
    }
}
