package es.rcs.tfm.main.setup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.rcs.tfm.api.controller.AccessController;
import es.rcs.tfm.api.model.Usuario;
import es.rcs.tfm.main.AppNames;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private final AuthenticationManager authenticationManager;

	public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
		
		this.authenticationManager = authenticationManager;
		setFilterProcessesUrl(AccessController.API_LOGIN_ACTION);
		
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {

		Usuario credenciales = null;
		
		if (request.getMethod().equals(HttpMethod.POST.toString())) {
			
			// Funciona con POST de parametros en HEADER
			if (	StringUtils.isNotBlank(request.getHeader(SPRING_SECURITY_FORM_USERNAME_KEY)) &&
					StringUtils.isNotBlank(request.getHeader(SPRING_SECURITY_FORM_PASSWORD_KEY)) ) {
				
				credenciales = new Usuario(
					request.getHeader(SPRING_SECURITY_FORM_USERNAME_KEY),
					request.getHeader(SPRING_SECURITY_FORM_PASSWORD_KEY));
				
			}
			
			if (	(credenciales == null) &&
					(StringUtils.isNotBlank(request.getHeader(AppNames.JWT_HEADER_AUTHORIZATION))) && 
					(request.getHeader(AppNames.JWT_HEADER_AUTHORIZATION).startsWith(AppNames.JWT_BASIC_TOKEN_PREFIX)) ) {
				
		        String token = request.getHeader(AppNames.JWT_HEADER_AUTHORIZATION);
		        String encodedString = token.replace(AppNames.JWT_BASIC_TOKEN_PREFIX, "");
				byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
				String decodedString = new String(decodedBytes);
				String[] params = decodedString.split("\\:");
				if ((params != null) && (params.length == 2)) {
					credenciales = new Usuario(
							params[0],
							params[1]
					);
				}
				
			} 
			
			if (	(credenciales == null) ) {
				
				// Funciona con POST y BODY RAW
				try {
					credenciales = new ObjectMapper()
							.configure(Feature.AUTO_CLOSE_SOURCE, true)
							.readValue(request.getInputStream(), Usuario.class);
					if (	StringUtils.isBlank(credenciales.getUsername()) ||
							StringUtils.isBlank(credenciales.getPassword()) ) {
						credenciales = null;
					}
				} catch (IOException e) {
				}
				
			}
		}
		
		if (credenciales != null) {
			try {
				return authenticationManager.authenticate(
						new UsernamePasswordAuthenticationToken(
								credenciales.getUsername(), 
								credenciales.getPassword(), 
								new ArrayList<>()));
			} catch (AuthenticationException e) {
				throw e;
			}
		} else {
			throw new BadCredentialsException("Credenciales no válidas");
		}
		
	}

	@Override
	protected void successfulAuthentication(
			HttpServletRequest request, 
			HttpServletResponse response,
			FilterChain filterChain, 
			Authentication authentication) {
		
		String username = null;
		
		if (authentication instanceof UsernamePasswordAuthenticationToken) {
			username = authentication.getName();
		} else {
			User user = ((User) authentication.getPrincipal());
			username = user.getUsername();
		}
		
		List<String> authorities = authentication
				.getAuthorities()
				.stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.toList());

		byte[] signingKey = AppNames.JWT_SECRET.getBytes();

		String token = Jwts
				.builder()
				.signWith(
						Keys.hmacShaKeyFor(signingKey), 
						SignatureAlgorithm.HS512)
				.setId(AppNames.JWT_ID)
				.setHeaderParam(AppNames.JWT_TYPE, AppNames.JWT_TOKENTYPE)
				.setIssuer(AppNames.JWT_ISSUER)
				.setAudience(AppNames.JWT_AUDIENCE)
				.setSubject(username)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + 864000000))
				.claim(AppNames.JWT_AUTHORITIES, authorities)
				.compact();

		response.addHeader(AppNames.JWT_HEADER_AUTHORIZATION, AppNames.JWT_BEARER_TOKEN_PREFIX + token);
		
	}
}