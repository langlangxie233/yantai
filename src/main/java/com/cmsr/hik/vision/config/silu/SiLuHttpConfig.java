package com.cmsr.hik.vision.config.silu;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class SiLuHttpConfig {
    @Value("${silu.url}")
    private String url;
}
