package it.wiesner.db.rls.datasource;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.ConnectionProxy;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import it.wiesner.db.rls.dialect.DatabaseDialect;
import it.wiesner.db.rls.session.RlsSessionHolder;
import jakarta.annotation.Nullable;

/**
 * Tenant-Aware Datasource that decorates Connections with current tenant
 * information.
 */
public class TenantAwareDataSource extends DelegatingDataSource {

	private static final Logger log = LoggerFactory.getLogger(TenantAwareDataSource.class);
	private final DatabaseDialect databaseDialect;

	public TenantAwareDataSource(DataSource targetDataSource, DatabaseDialect databaseDialect) {
		super(targetDataSource);
		this.databaseDialect = databaseDialect;
		log.info("TenantAwareDataSource initialized with dialect: {}", databaseDialect.getClass().getSimpleName());
	}

	@SuppressWarnings("null")
	@Override
	public Connection getConnection() throws SQLException {
		final Connection connection = getTargetDataSource().getConnection();
		setTenantId(connection);
		log.debug("Created new database connection with tenant context");
		return getTenantAwareConnectionProxy(connection);
	}

	@SuppressWarnings("null")
	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		final Connection connection = getTargetDataSource().getConnection(username, password);
		setTenantId(connection);
		log.debug("Created new database connection with credentials and tenant context");
		return getTenantAwareConnectionProxy(connection);
	}

	private void setTenantId(Connection connection) throws SQLException {
		if (RlsSessionHolder.getRlsSession() != null) {
			Long tenantId = RlsSessionHolder.getRlsSession().tenantId;
			log.info("Setting tenant context - TenantId: {}", tenantId);
			databaseDialect.setTenantContext(connection, tenantId);
		} else {
			log.warn("No RLS session found when acquiring connection");
		}
	}

	private void clearTenantId(Connection connection) throws SQLException {
		log.info("Clearing tenant context from connection");
		databaseDialect.clearTenantContext(connection);
	}

	// Connection Proxy that intercepts close() to reset the tenant_id
	protected Connection getTenantAwareConnectionProxy(Connection connection) {
		return (Connection) Proxy.newProxyInstance(ConnectionProxy.class.getClassLoader(),
				new Class[] { ConnectionProxy.class },
				new TenantAwareDataSource.TenantAwareInvocationHandler(connection));
	}

	// Connection Proxy invocation handler that intercepts close() to reset the
	// tenant_id
	private class TenantAwareInvocationHandler implements InvocationHandler {
		private final Connection target;

		public TenantAwareInvocationHandler(Connection target) {
			this.target = target;
		}

		@Override
		@Nullable
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			switch (method.getName()) {
			case "equals":
				return proxy == args[0];
			case "hashCode":
				return System.identityHashCode(proxy);
			case "toString":
				return "Tenant-aware proxy for target Connection [" + this.target.toString() + "]";
			case "unwrap":
				if (((Class<?>) args[0]).isInstance(proxy)) {
					return proxy;
				} else {
					return method.invoke(target, args);
				}
			case "isWrapperFor":
				if (((Class<?>) args[0]).isInstance(proxy)) {
					return true;
				} else {
					return method.invoke(target, args);
				}
			case "getTargetConnection":
				return target;
			default:
				if (method.getName().equals("close")) {
					clearTenantId(target);
				}
				return method.invoke(target, args);
			}
		}
	}
}
