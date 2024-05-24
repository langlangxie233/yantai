package com.cmsr.hik.vision.config.anjian;

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
public class AnJianDorisDataSourceConfig {
    @Bean(name = "anJianDorisDataSource")
    @ConfigurationProperties(prefix ="datasource.anjiandoris")
    public DataSource prestoDataSource() {
        return DataSourceBuilder.create().build();
    }
    @Bean(name = "anJianDorisTemplate")
    public JdbcTemplate dorisJdbcTemplate(@Qualifier("anJianDorisDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
