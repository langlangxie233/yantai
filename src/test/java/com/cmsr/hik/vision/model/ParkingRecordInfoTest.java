package com.cmsr.hik.vision.model;

import com.cmsr.hik.vision.model.parking.ParkingInfo;
import org.junit.jupiter.api.Test;

public class ParkingRecordInfoTest {
    @Test
    public void testJson() {
        ParkingInfo info = new ParkingInfo();
        //info.setAswSysCode("123");
        info.setCardNo("123");
        System.out.println(info.toString());
    }

}
