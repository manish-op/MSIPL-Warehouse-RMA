package com.serverManagement.server.management.utils.jwt;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JWTUtils {

	private String secret = "/sNoS83p0CVIcqW0iK71ytYDb1WdZLeD3JRDzeGlCYDwwBRZEn3RMt4JfPIruzya4RLjAlK3mxsUpxS4pxuSPQ==";
	public static final long JWT_TOKEN_VALIDITY = 6 * 24 * 60 * 60;

	Key key = new SecretKeySpec(secret.getBytes(), SignatureAlgorithm.HS256.getJcaName());

	// retrieve username from jwt token
	public String getUsernameFromToken(String token) {
		return getClaimFromToken(token, Claims::getSubject);
	}

	// retrieve expiration date from jwt token
	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}

	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

	// for retrieveing any information from token we will need the secret key
	private Claims getAllClaimsFromToken(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

	// check if the token has expired
	private Boolean isTokenExpired(String token) throws Exception {
		boolean isTokenExpired = false;
		Date todayDate = new Date();
		final Date expiration = getExpirationDateFromToken(token);
		if (expiration.before(todayDate)) {
			isTokenExpired = true;
			throw new ExpiredJwtException(null, null, token);
		}
		return isTokenExpired;
	}

	// generate token for user
	public String generateToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		return doGenerateToken(claims, userDetails.getUsername());
	}

	// generate token with custom claims
	public String generateToken(UserDetails userDetails, Map<String, Object> claims) {
		return doGenerateToken(claims, userDetails.getUsername());
	}

	private String doGenerateToken(Map<String, Object> claims, String subject) {

		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
				.signWith(key, SignatureAlgorithm.HS256).compact();
	}

	// validate token
	public Boolean validateToken(String token, UserDetails userDetails) throws Exception {
		String username = getUsernameFromToken(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}

}
