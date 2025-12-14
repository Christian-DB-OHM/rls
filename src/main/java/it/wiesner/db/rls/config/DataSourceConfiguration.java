package it.wiesner.db.rls.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.zaxxer.hikari.HikariDataSource;

import it.wiesner.db.rls.datasource.TenantAwareDataSource;
import it.wiesner.db.rls.dialect.DatabaseDialect;
import it.wiesner.db.rls.dialect.MssqlDialect;
import it.wiesner.db.rls.dialect.PostgresDialect;

/**
 * Configuration class for setting up the TenantAwareDataSource with Row Level Security support.
 */
@Configuration
public class DataSourceConfiguration {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfiguration.class);

    @Value("${app.database.type:postgres}")
    private String databaseType;

    /**
     * Creates the appropriate DatabaseDialect based on the configured database type.
     */
    @Bean
    public DatabaseDialect databaseDialect() {
        log.info("Configuring database dialect for type: {}", databaseType);
        
        DatabaseDialect dialect;
        if ("mssql".equalsIgnoreCase(databaseType)) {
            log.info("Using Microsoft SQL Server dialect");
            dialect = new MssqlDialect();
        } else {
            log.info("Using PostgreSQL dialect");
            dialect = new PostgresDialect();
        }
        
        return dialect;
    }

    /**
     * Creates the actual underlying DataSource using Spring Boot's DataSourceProperties.
     */
    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource actualDataSource(DataSourceProperties properties) {
        log.info("Creating actual DataSource with properties");
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    /**
     * Wraps the actual DataSource with TenantAwareDataSource to enable Row Level Security.
     * This bean is marked as @Primary so it will be used by Spring Data JPA.
     */
    @Bean
    @Primary
    public DataSource dataSource(@Qualifier("actualDataSource") DataSource actualDataSource, 
                                  DatabaseDialect databaseDialect) {
        log.info("Creating TenantAwareDataSource with {} dialect", databaseDialect.getClass().getSimpleName());
        
        TenantAwareDataSource tenantAwareDataSource = new TenantAwareDataSource(actualDataSource, databaseDialect);
        
        log.info("TenantAwareDataSource successfully configured and ready for use");
        return tenantAwareDataSource;
    }
}
