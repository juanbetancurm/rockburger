package com.rockburger.burgermain.adapters.driving.http.controller.auth;

import com.rockburger.burgermain.adapters.driving.http.dto.request.LoginRequestDto;
import com.rockburger.burgermain.adapters.driving.http.dto.response.LoginResponseDto;
import com.rockburger.burgermain.adapters.driving.http.mapper.IAuthenticationResponseMapper;
import com.rockburger.burgermain.domain.api.IAuthenticationServicePort;
import com.rockburger.burgermain.domain.api.IJwtServicePort;
import com.rockburger.burgermain.domain.exception.InvalidCredentialsException;
import com.rockburger.burgermain.domain.exception.InvalidTokenException;
import com.rockburger.burgermain.domain.exception.NotFoundException;
import com.rockburger.burgermain.domain.model.JwtModel;
import com.rockburger.burgermain.domain.model.UserModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@Tag(name = "Authentication", description = "User authentication operations")
public class AuthenticationController {

	private final IAuthenticationServicePort authenticationServicePort;
	private final IAuthenticationResponseMapper authenticationResponseMapper;
	private final IJwtServicePort jwtServicePort;

	public AuthenticationController(
			IAuthenticationServicePort authenticationServicePort,
			IAuthenticationResponseMapper authenticationResponseMapper,
			IJwtServicePort jwtServicePort) {
		this.authenticationServicePort = authenticationServicePort;
		this.authenticationResponseMapper = authenticationResponseMapper;
		this.jwtServicePort = jwtServicePort;
	}

	@PostMapping("/login")
	@Operation(
			summary = "User login",
			description = "Authenticates user credentials and returns access and refresh tokens",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "Login successful",
							content = @Content(schema = @Schema(implementation = EnhancedLoginResponse.class))
					),
					@ApiResponse(
							responseCode = "401",
							description = "Invalid credentials"
					),
					@ApiResponse(
							responseCode = "404",
							description = "User not found"
					),
					@ApiResponse(
							responseCode = "400",
							description = "Invalid request format"
					)
			}
	)
	public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto loginRequest) {
		log.info("Login attempt for user: {}", loginRequest.getEmail());

		try {
			// Validate request inputs
			if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
				log.warn("Login request missing email");
				return createErrorResponse("Email is required", HttpStatus.BAD_REQUEST);
			}

			if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
				log.warn("Login request missing password for user: {}", loginRequest.getEmail());
				return createErrorResponse("Password is required", HttpStatus.BAD_REQUEST);
			}

			// Try enhanced authentication first, fall back to standard if not available
			UserModel authenticatedUser = null;
			JwtModel jwtModel = null;

			try {
				// Try to use enhanced authentication method if available
				authenticatedUser = authenticationServicePort.authenticateAndGetUser(
						loginRequest.getEmail().trim(),
						loginRequest.getPassword()
				);
				log.debug("Enhanced authentication successful for user: {}", authenticatedUser.getEmail());
			} catch (NoSuchMethodError | AbstractMethodError e) {
				// Enhanced method not available, fall back to standard authentication
				log.debug("Enhanced authentication not available, using standard authentication");
			} catch (UnsupportedOperationException e) {
				// Method exists but throws UnsupportedOperationException, fall back to standard
				log.debug("Enhanced authentication not implemented, using standard authentication");
			} catch (Exception e) {
				// Enhanced authentication failed, will handle error below
				log.debug("Enhanced authentication failed: {}", e.getMessage());
				throw e;
			}

			// If enhanced authentication didn't work, use standard authentication
			if (authenticatedUser == null) {
				jwtModel = authenticationServicePort.authenticate(
						loginRequest.getEmail().trim(),
						loginRequest.getPassword()
				);
				log.debug("Standard authentication successful");
			}

			// Try to generate token pair if JWT service supports it
			if (authenticatedUser != null) {
				try {
					IJwtServicePort.JwtTokenPair tokenPair = jwtServicePort.generateTokenPair(authenticatedUser);

					// Create enhanced response with both tokens
					EnhancedLoginResponse response = new EnhancedLoginResponse(
							tokenPair.getAccessToken(),
							tokenPair.getRefreshToken(),
							"Bearer",
							authenticatedUser.getId(),
							authenticatedUser.getEmail(),
							authenticatedUser.getRole(),
							tokenPair.getAccessTokenExpiration(),
							tokenPair.getRefreshTokenExpiration(),
							"Login successful"
					);

					log.info("Login successful for user: {} with role: {} (enhanced mode)",
							authenticatedUser.getEmail(), authenticatedUser.getRole());

					return ResponseEntity.ok(response);

				} catch (NoSuchMethodError | AbstractMethodError e) {
					log.debug("Token pair generation not supported, falling back to single token");
					// Generate standard JWT token
					jwtModel = jwtServicePort.generateToken(authenticatedUser);
				} catch (Exception e) {
					log.debug("Token pair generation failed, falling back to single token: {}", e.getMessage());
					// Generate standard JWT token
					jwtModel = jwtServicePort.generateToken(authenticatedUser);
				}
			}

			// Fall back to standard response
			if (jwtModel != null) {
				LoginResponseDto response = authenticationResponseMapper.toDto(jwtModel);
				log.info("Login successful for user: {} (standard mode)", loginRequest.getEmail());
				return ResponseEntity.ok(response);
			}

			// This should not happen, but just in case
			throw new RuntimeException("Authentication succeeded but no token was generated");

		} catch (NotFoundException e) {
			log.warn("Login failed - user not found: {}", loginRequest.getEmail());
			return createErrorResponse("User not found", HttpStatus.NOT_FOUND);
		} catch (InvalidCredentialsException e) {
			log.warn("Login failed - invalid credentials for user: {}", loginRequest.getEmail());
			return createErrorResponse("Invalid email or password", HttpStatus.UNAUTHORIZED);
		} catch (Exception e) {
			log.error("Unexpected error during login for user {}: {}", loginRequest.getEmail(), e.getMessage(), e);
			return createErrorResponse("Login failed due to server error", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/refresh")
	@Operation(
			summary = "Refresh access token",
			description = "Refreshes an expired access token using a valid refresh token",
			responses = {
					@ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
					@ApiResponse(responseCode = "401", description = "Invalid or expired refresh token"),
					@ApiResponse(responseCode = "400", description = "Refresh token is required"),
					@ApiResponse(responseCode = "501", description = "Token refresh not supported")
			}
	)
	public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
		log.debug("Received token refresh request");

		try {
			if (request == null || request.getRefreshToken() == null || request.getRefreshToken().trim().isEmpty()) {
				log.warn("Token refresh request missing refresh token");
				return createErrorResponse("Refresh token is required", HttpStatus.BAD_REQUEST);
			}

			// Check if refresh method is available
			try {
				JwtModel newToken = jwtServicePort.refreshToken(request.getRefreshToken());

				RefreshTokenResponse response = new RefreshTokenResponse(
						newToken.getToken(),
						newToken.getUserId(),
						newToken.getEmail(),
						newToken.getRole(),
						"Token refreshed successfully"
				);

				log.info("Token refreshed successfully for user: {}", newToken.getEmail());
				return ResponseEntity.ok(response);

			} catch (NoSuchMethodError | AbstractMethodError e) {
				log.warn("Token refresh method not available");
				return createErrorResponse("Token refresh not supported", HttpStatus.NOT_IMPLEMENTED);
			} catch (InvalidTokenException e) {
				log.warn("Token refresh failed - invalid token: {}", e.getMessage());
				return createErrorResponse("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED);
			}

		} catch (Exception e) {
			log.warn("Token refresh failed: {}", e.getMessage());
			return createErrorResponse("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED);
		}
	}

	@PostMapping("/validate")
	@Operation(
			summary = "Validate access token",
			description = "Validates an access token and returns token status information",
			responses = {
					@ApiResponse(responseCode = "200", description = "Token validation successful"),
					@ApiResponse(responseCode = "401", description = "Invalid or expired token"),
					@ApiResponse(responseCode = "400", description = "Token is required"),
					@ApiResponse(responseCode = "501", description = "Token validation not supported")
			}
	)
	public ResponseEntity<?> validateToken(@Valid @RequestBody TokenValidationRequest request) {
		log.debug("Received token validation request");

		try {
			String token = request.getToken();
			if (token == null || token.trim().isEmpty()) {
				return createErrorResponse("Token is required", HttpStatus.BAD_REQUEST);
			}

			try {
				// Check if token is expired
				boolean isExpired = jwtServicePort.isTokenExpired(token);
				if (isExpired) {
					return createErrorResponse("Token is expired", HttpStatus.UNAUTHORIZED);
				}

				// Check if token is expiring soon
				boolean isExpiringSoon = jwtServicePort.isTokenExpiringSoon(token);

				// Get token expiration time
				long expirationTime = jwtServicePort.getTokenExpirationTime(token);

				// Get user email from token
				String email = jwtServicePort.extractUserEmailFromToken(token);

				TokenValidationResponse response = new TokenValidationResponse(
						true,
						!isExpired,
						isExpiringSoon,
						expirationTime,
						email,
						"Token is valid"
				);

				return ResponseEntity.ok(response);

			} catch (NoSuchMethodError | AbstractMethodError e) {
				log.warn("Token validation methods not available");
				return createErrorResponse("Token validation not supported", HttpStatus.NOT_IMPLEMENTED);
			}

		} catch (InvalidTokenException e) {
			log.warn("Token validation failed: {}", e.getMessage());
			return createErrorResponse("Invalid token", HttpStatus.UNAUTHORIZED);
		} catch (Exception e) {
			log.error("Unexpected error during token validation: {}", e.getMessage(), e);
			return createErrorResponse("Token validation failed", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/logout")
	@Operation(
			summary = "User logout",
			description = "Logs out the current user (client-side token removal)"
	)
	public ResponseEntity<LogoutResponse> logout() {
		log.info("User logout request received");

		LogoutResponse response = new LogoutResponse(
				"Logout successful. Please remove tokens from client storage.",
				true
		);

		return ResponseEntity.ok(response);
	}

	private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
		Map<String, Object> errorResponse = new HashMap<>();
		errorResponse.put("error", true);
		errorResponse.put("message", message);
		errorResponse.put("status", status.value());
		errorResponse.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

		return new ResponseEntity<>(errorResponse, status);
	}

	// Response DTOs for Enhanced Token Support

	public static class EnhancedLoginResponse {
		private final String token;
		private final String refreshToken;
		private final String type;
		private final Long userId;
		private final String email;
		private final String role;
		private final long accessTokenExpiration;
		private final long refreshTokenExpiration;
		private final String message;

		public EnhancedLoginResponse(String token, String refreshToken, String type, Long userId,
									 String email, String role, long accessTokenExpiration,
									 long refreshTokenExpiration, String message) {
			this.token = token;
			this.refreshToken = refreshToken;
			this.type = type;
			this.userId = userId;
			this.email = email;
			this.role = role;
			this.accessTokenExpiration = accessTokenExpiration;
			this.refreshTokenExpiration = refreshTokenExpiration;
			this.message = message;
		}

		// Getters
		public String getToken() { return token; }
		public String getRefreshToken() { return refreshToken; }
		public String getType() { return type; }
		public Long getUserId() { return userId; }
		public String getEmail() { return email; }
		public String getRole() { return role; }
		public long getAccessTokenExpiration() { return accessTokenExpiration; }
		public long getRefreshTokenExpiration() { return refreshTokenExpiration; }
		public String getMessage() { return message; }
	}

	public static class RefreshTokenRequest {
		private String refreshToken;

		public RefreshTokenRequest() {}
		public RefreshTokenRequest(String refreshToken) { this.refreshToken = refreshToken; }

		public String getRefreshToken() { return refreshToken; }
		public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
	}

	public static class RefreshTokenResponse {
		private final String accessToken;
		private final Long userId;
		private final String email;
		private final String role;
		private final String message;

		public RefreshTokenResponse(String accessToken, Long userId, String email, String role, String message) {
			this.accessToken = accessToken;
			this.userId = userId;
			this.email = email;
			this.role = role;
			this.message = message;
		}

		public String getAccessToken() { return accessToken; }
		public Long getUserId() { return userId; }
		public String getEmail() { return email; }
		public String getRole() { return role; }
		public String getMessage() { return message; }
	}

	public static class TokenValidationRequest {
		private String token;

		public TokenValidationRequest() {}
		public TokenValidationRequest(String token) { this.token = token; }

		public String getToken() { return token; }
		public void setToken(String token) { this.token = token; }
	}

	public static class TokenValidationResponse {
		private final boolean valid;
		private final boolean active;
		private final boolean expiringSoon;
		private final long expirationTime;
		private final String email;
		private final String message;

		public TokenValidationResponse(boolean valid, boolean active, boolean expiringSoon,
									   long expirationTime, String email, String message) {
			this.valid = valid;
			this.active = active;
			this.expiringSoon = expiringSoon;
			this.expirationTime = expirationTime;
			this.email = email;
			this.message = message;
		}

		public boolean isValid() { return valid; }
		public boolean isActive() { return active; }
		public boolean isExpiringSoon() { return expiringSoon; }
		public long getExpirationTime() { return expirationTime; }
		public String getEmail() { return email; }
		public String getMessage() { return message; }
	}

	public static class LogoutResponse {
		private final String message;
		private final boolean success;

		public LogoutResponse(String message, boolean success) {
			this.message = message;
			this.success = success;
		}

		public String getMessage() { return message; }
		public boolean isSuccess() { return success; }
	}
}