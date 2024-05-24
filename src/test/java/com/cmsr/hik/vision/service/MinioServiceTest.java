package com.cmsr.hik.vision.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
public class MinioServiceTest {
    @Autowired
    private MinioService service;

    @Test
    public void testGetObject() {
        Map<String, String> objects = service.getObjects("2024-04-18/");
        objects.forEach((k, v) -> System.out.println(k + ":" + v));
    }
}
