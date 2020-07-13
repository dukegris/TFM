package es.rcs.tfm.main.setup;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import es.rcs.tfm.main.AppNames;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthorizationFilter.class);

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(
    		HttpServletRequest request, 
    		HttpServletResponse response,
            FilterChain filterChain) throws IOException, ServletException {
    	
    	String header = request.getHeader(AppNames.JWT_HEADER_AUTHORIZATION);
		if (header == null || !header.startsWith(AppNames.JWT_BEARER_TOKEN_PREFIX)) {
			filterChain.doFilter(request, response);
			return;
		}
		
    	UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        if (authentication == null) {
            filterChain.doFilter(request, response);
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        filterChain.doFilter(request, response);
        
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
    	
        String token = request.getHeader(AppNames.JWT_HEADER_AUTHORIZATION);
        if (StringUtils.isNotBlank(token) && token.startsWith(AppNames.JWT_BEARER_TOKEN_PREFIX)) {
            try {
            	
                byte[] signingKey = AppNames.JWT_SECRET.getBytes();

                Jws<Claims> parsedToken = Jwts
                		.parser()
	                    .setSigningKey(signingKey)
	                    .parseClaimsJws(token.replace(AppNames.JWT_BEARER_TOKEN_PREFIX, ""));

                String username = parsedToken
                    .getBody()
                    .getSubject();

                List<SimpleGrantedAuthority> authorities = 
                	((List<?>) parsedToken
	                		.getBody()
		                    .get(AppNames.JWT_AUTHORITIES))
                		.stream()
		                    .map(authority -> new SimpleGrantedAuthority((String) authority))
		                    .collect(Collectors.toList());

                if (StringUtils.isNotBlank(username)) {
                    return new UsernamePasswordAuthenticationToken(
                    		username, 
                    		null, 
                    		authorities);

                }
                
            } catch (ExpiredJwtException exception) {
                log.warn("Request to parse expired JWT : {} failed : {}", token, exception.getMessage());
            } catch (UnsupportedJwtException exception) {
                log.warn("Request to parse unsupported JWT : {} failed : {}", token, exception.getMessage());
            } catch (MalformedJwtException exception) {
                log.warn("Request to parse invalid JWT : {} failed : {}", token, exception.getMessage());
            } catch (IllegalArgumentException exception) {
                log.warn("Request to parse empty or null JWT : {} failed : {}", token, exception.getMessage());
            }
        }

        return null;
    }
}