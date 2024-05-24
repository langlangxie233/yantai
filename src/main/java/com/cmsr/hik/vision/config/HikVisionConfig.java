package com.cmsr.hik.vision.config;

import com.hikvision.artemis.sdk.config.ArtemisConfig;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class HikVisionConfig {
    @Value("${hik.host}")
    private String host;
    @Value("${hik.app-key}")
    private String appKey;
    @Value("${hik.app-secret}")
    private String appSecret;

    public ArtemisConfig getConfig() {
        ArtemisConfig config = new ArtemisConfig();
        config.setHost(host);
        config.setAppKey(appKey);
        config.setAppSecret(appSecret);

        return config;
    }
}
