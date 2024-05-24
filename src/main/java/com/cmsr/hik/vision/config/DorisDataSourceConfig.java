package com.cmsr.hik.vision.config;

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
public class DorisDataSourceConfig {
    @Bean(name = "dorisDataSource")
    @ConfigurationProperties(prefix ="datasource.doris")
    public DataSource prestoDataSource() {
        return DataSourceBuilder.create().build();
    }
    @Bean(name = "dorisTemplate")
    public JdbcTemplate dorisJdbcTemplate(@Qualifier("dorisDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
