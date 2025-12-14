package it.wiesner.db.rls.dialect;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PostgreSQL implementation of DatabaseDialect.
 * Uses custom configuration parameters for Row Level Security.
 */
public class PostgresDialect implements DatabaseDialect {
    
    private static final Logger logger = LoggerFactory.getLogger(PostgresDialect.class);

    @Override
    public void setTenantContext(Connection connection, Long tenantId) throws SQLException {
        try (Statement sql = connection.createStatement()) {
            String setTenantId = String.format("SET app.tenant_id = '%d'", tenantId);
            
            logger.debug("Setting PostgreSQL tenant context: TenantId={}", tenantId);
            sql.execute(setTenantId);
        }
    }

    @Override
    public void clearTenantContext(Connection connection) throws SQLException {
        try (Statement sql = connection.createStatement()) {
            logger.debug("Clearing PostgreSQL tenant context");
            sql.execute("RESET app.tenant_id");
        }
    }
}
