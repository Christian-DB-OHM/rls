package it.wiesner.db.rls.dialect;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Microsoft SQL Server implementation of DatabaseDialect.
 * Uses SESSION_CONTEXT for Row Level Security.
 */
public class MssqlDialect implements DatabaseDialect {
    
    private static final Logger logger = LoggerFactory.getLogger(MssqlDialect.class);

    @Override
    public void setTenantContext(Connection connection, Long tenantId) throws SQLException {
        try (Statement sql = connection.createStatement()) {
            String setTenantId = String.format(
                "EXEC sys.sp_set_session_context @key = N'TenantId', @value = %d", 
                tenantId
            );
            
            logger.debug("Setting MSSQL tenant context: TenantId={}", tenantId);
            sql.execute(setTenantId);
        }
    }

    @Override
    public void clearTenantContext(Connection connection) throws SQLException {
        try (Statement sql = connection.createStatement()) {
            logger.debug("Clearing MSSQL tenant context");
            sql.execute("EXEC sys.sp_set_session_context @key = N'TenantId', @value = -1");
        }
    }
}
