package it.wiesner.db.rls.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import it.wiesner.db.rls.interceptor.RlsSessionInterceptor;
import it.wiesner.db.rls.session.RlsSessionHolder;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class LoginController {

	Logger log = LoggerFactory.getLogger(LoginController.class);

	@PostMapping("/rls/login")
	public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
		try {
			log.info("Login request received for tenant: {}, username: {}", 
				loginRequest.getTenantId(), loginRequest.getUsername());

			boolean success = performLogin(loginRequest.getTenantId(), loginRequest.getUsername(), request);
			
			if (success) {
				return ResponseEntity.ok(new LoginResponse(true, "Login successful"));
			} else {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new LoginResponse(false, "Invalid credentials"));
			}
		} catch (Exception e) {
			log.error("Error during login", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new LoginResponse(false, "Internal server error"));
		}
	}

	@PostMapping("/rls/logout")
	public ResponseEntity<LoginResponse> logout(HttpServletRequest request) {
		try {
			performLogout(request);
			return ResponseEntity.ok(new LoginResponse(true, "Logout successful"));
		} catch (Exception e) {
			log.error("Error during logout", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new LoginResponse(false, "Internal server error"));
		}
	}

	/**
	 * Authenticates a user and sets up the RLS session
	 * @param tenantId The tenant ID
	 * @param username The username
	 * @param request The HTTP request
	 * @return true if login successful
	 */
	private boolean performLogin(Long tenantId, String username, HttpServletRequest request) {
		log.info("Login attempt for tenant: {}, username: {}", tenantId, username);
		
		// In a real application, you would validate credentials here
		// For this demo, we accept any non-null values
		if (tenantId != null && username != null && !username.trim().isEmpty()) {
			// Store the RLS session in HTTP session (persists across requests)
			RlsSessionInterceptor.storeInHttpSession(request, tenantId);
			// Also set in ThreadLocal for immediate use
			RlsSessionHolder.setRlsSession(tenantId);
			log.info("Login successful for tenant: {}, username: {}", tenantId, username);
			return true;
		}
		
		log.warn("Login failed for tenant: {}, username: {}", tenantId, username);
		return false;
	}

	/**
	 * Logs out the current user by clearing the RLS session
	 * @param request The HTTP request
	 */
	private void performLogout(HttpServletRequest request) {
		log.info("Logging out user");
		// Remove from HTTP session
		RlsSessionInterceptor.removeFromHttpSession(request);
		// Clear ThreadLocal
		RlsSessionHolder.clear();
	}

	// Inner classes for request/response
	public static class LoginRequest {
		private Long tenantId;
		private String username;

		public Long getTenantId() {
			return tenantId;
		}

		public void setTenantId(Long tenantId) {
			this.tenantId = tenantId;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}
	}

	public static class LoginResponse {
		private boolean success;
		private String message;

		public LoginResponse(boolean success, String message) {
			this.success = success;
			this.message = message;
		}

		public boolean isSuccess() {
			return success;
		}

		public void setSuccess(boolean success) {
			this.success = success;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

}
