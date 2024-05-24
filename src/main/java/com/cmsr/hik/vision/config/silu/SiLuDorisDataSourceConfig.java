package com.cmsr.hik.vision.config.silu;

import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Data
@Configuration
public class SiLuDorisDataSourceConfig {
    @Bean(name = "siLuDorisDataSource")
    @ConfigurationProperties(prefix ="datasource.siludoris")
    public DataSource prestoDataSource() {
        return DataSourceBuilder.create().build();
    }
    @Bean(name = "siLuDorisTemplate")
    public JdbcTemplate dorisJdbcTemplate(@Qualifier("siLuDorisDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
