package com.cmsr.hik.vision.config.anjian;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class AnJianHttpConfig {
    @Value("${anjian.url}")
    private String url;
    @Value("${anjian.mobile-phone}")
    private String mobilePhone;
    @Value("${anjian.key}")
    private String key;
    @Value("${anjian.secret}")
    private String secret;
}
