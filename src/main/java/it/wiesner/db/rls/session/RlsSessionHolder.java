package it.wiesner.db.rls.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RlsSessionHolder {

	private static final Logger log = LoggerFactory.getLogger(RlsSessionHolder.class);
	private static final InheritableThreadLocal<RlsSession> session = new InheritableThreadLocal<>();

	public static void setRlsSession(Long tenantId) {
		session.set(new RlsSession(tenantId));
		log.info("RLS session set - TenantId: {}", tenantId);
	}

	public static RlsSession getRlsSession() {
		RlsSession currentSession = session.get();
		if (currentSession != null) {
			log.debug("Retrieved RLS session - TenantId: {}", currentSession.tenantId);
		} else {
			log.debug("No RLS session found in current thread");
		}
		return currentSession;
	}

	public static void clear() {
		RlsSession currentSession = session.get();
		if (currentSession != null) {
			log.info("Clearing RLS session - Previous TenantId: {}", currentSession.tenantId);
		}
		session.remove();
	}

	public static class RlsSession implements java.io.Serializable {
		
		private static final long serialVersionUID = 1L;

		public RlsSession(Long tenantId) {
			this.tenantId = tenantId;
		}

		public Long tenantId;

	}

}
