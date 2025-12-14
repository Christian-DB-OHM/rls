package it.wiesner.db.rls.dialect;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Strategy interface for database-specific session context handling.
 */
public interface DatabaseDialect {
    
    /**
     * Set the tenant context variables in the database session.
     * 
     * @param connection the database connection
     * @param tenantId the tenant ID
     * @throws SQLException if the SQL execution fails
     */
    void setTenantContext(Connection connection, Long tenantId) throws SQLException;
    
    /**
     * Clear the tenant context variables in the database session.
     * 
     * @param connection the database connection
     * @throws SQLException if the SQL execution fails
     */
    void clearTenantContext(Connection connection) throws SQLException;
}
