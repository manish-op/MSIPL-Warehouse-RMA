package com.serverManagement.server.management.filter;

import java.io.IOException;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.serverManagement.server.management.dao.admin.user.AdminUserDAO;
import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;
import com.serverManagement.server.management.service.admin.login.AdminUserLogin;
import com.serverManagement.server.management.utils.jwt.JWTUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JWTRequestFilter extends OncePerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(JWTRequestFilter.class);

	@Autowired
	private JWTUtils jwtUtils;

	@Autowired
	private AdminUserLogin loginService;

	@Autowired
	private AdminUserDAO adminUserDAO;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		final String authorizationHadder = request.getHeader("Authorization");
		String userName = null;
		String jwt = null;
		if (authorizationHadder != null && authorizationHadder.startsWith("Bearer ")) {
			try {

				jwt = authorizationHadder.substring(7);

				// Validate JWT token is not empty or blank
				if (jwt == null || jwt.trim().isEmpty()) {
					logger.warn("JWT token is empty or blank");
					filterChain.doFilter(request, response);
					return;
				}

				userName = jwtUtils.getUsernameFromToken(jwt);

				if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
					UserDetails userDetails = this.loginService.loadUserByUsername(userName);
					if (jwtUtils.validateToken(jwt, userDetails)) {

						UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
								userDetails, null, userDetails.getAuthorities());
						usernamePasswordAuthenticationToken
								.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						// After setting the Authentication in the context, we specify
						// that the current user is authenticated. So it passes the
						// Spring Security Configurations successfully.
						SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

						// Update last active timestamp for online status tracking
						try {
							AdminUserEntity user = adminUserDAO.findByEmail(userName.toLowerCase());
							if (user != null) {
								user.setLastActiveAt(ZonedDateTime.now());
								adminUserDAO.save(user);
							}
						} catch (Exception e) {
							logger.warn("Failed to update last active time: {}", e.getMessage());
						}
					}
				}

			} catch (ExpiredJwtException exception) {
				logger.error("JWT token has expired: {}", exception.getMessage());
				throw new ExpiredJwtException(null, null, "Session Expired", exception);
			} catch (MalformedJwtException exception) {
				logger.error("Malformed JWT token: {}", exception.getMessage());
				// Don't throw, just log and continue the filter chain
				// This allows the request to proceed but without authentication
			} catch (UsernameNotFoundException e) {
				logger.error("User not found: {}", e.getMessage());
				throw new UsernameNotFoundException("Invalid User Details");
			} catch (Exception exception) {
				logger.error("Error processing JWT token: {}", exception.getMessage());
				exception.printStackTrace();
			}
		}
		filterChain.doFilter(request, response);
	}

}
