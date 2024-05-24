package com.cmsr.hik.vision;

import com.cmsr.hik.vision.config.HikVisionConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CollectMainTest {
    @Autowired
    private HikVisionConfig config;

    @Test
    public void loadConfig() {
        System.out.println(config.getConfig().toString());
    }
}
