package de.maxhenkel.modupdateserver.configs;

import de.maxhenkel.modupdateserver.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Profile("prod")
@Configuration
public class DataSourceConfig {

    @Autowired
    private DataSourceProperties dataSourceProperties;

    @Bean
    public DataSource getDataSource() {
        DataSourceBuilder<?> dataSourceBuilder = dataSourceProperties.initializeDataSourceBuilder();
        dataSourceBuilder.url("jdbc:postgresql://%s:%s/%s".formatted(Utils.getEnv("DB_IP", "localhost"), Utils.getEnv("DB_PORT", "5432"), Utils.getEnv("DB_NAME", "postgres")));
        dataSourceBuilder.username(Utils.getEnv("DB_USER", "postgres"));
        dataSourceBuilder.password(Utils.getEnv("DB_PASSWORD", ""));
        return dataSourceBuilder.build();
    }

}
