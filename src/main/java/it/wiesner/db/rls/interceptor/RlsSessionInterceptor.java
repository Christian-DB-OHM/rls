package it.wiesner.db.rls.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import it.wiesner.db.rls.session.RlsSessionHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Interceptor that copies RLS session from HTTP session to ThreadLocal before each request.
 * This ensures that the session set during login is available for all subsequent requests.
 */
@Component
public class RlsSessionInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RlsSessionInterceptor.class);
    private static final String RLS_SESSION_ATTR = "RLS_SESSION";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession httpSession = request.getSession(false);
        
        if (httpSession != null) {
            RlsSessionHolder.RlsSession rlsSession = 
                (RlsSessionHolder.RlsSession) httpSession.getAttribute(RLS_SESSION_ATTR);
            
            if (rlsSession != null) {
                // Copy session from HTTP session to ThreadLocal
                RlsSessionHolder.setRlsSession(rlsSession.tenantId);
                log.debug("Copied RLS session to ThreadLocal - TenantId: {}", rlsSession.tenantId);
            } else {
                log.debug("No RLS session found in HTTP session for request: {}", request.getRequestURI());
            }
        } else {
            log.debug("No HTTP session for request: {}", request.getRequestURI());
        }
        
        return true;
    }

    @SuppressWarnings("null")
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                 Object handler, Exception ex) {
        // Clean up ThreadLocal after request completes
        RlsSessionHolder.clear();
        log.debug("Cleared ThreadLocal RLS session after request completion");
    }

    /**
     * Store RLS session in HTTP session
     */
    public static void storeInHttpSession(HttpServletRequest request, Long tenantId) {
        HttpSession httpSession = request.getSession(true);
        RlsSessionHolder.RlsSession rlsSession = new RlsSessionHolder.RlsSession(tenantId);
        httpSession.setAttribute(RLS_SESSION_ATTR, rlsSession);
        log.info("Stored RLS session in HTTP session - TenantId: {}", tenantId);
    }

    /**
     * Remove RLS session from HTTP session
     */
    public static void removeFromHttpSession(HttpServletRequest request) {
        HttpSession httpSession = request.getSession(false);
        if (httpSession != null) {
            httpSession.removeAttribute(RLS_SESSION_ATTR);
            httpSession.invalidate();
            log.info("Removed RLS session from HTTP session");
        }
    }
}
