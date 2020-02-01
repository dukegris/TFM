package es.rcs.tfm.main.components;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import es.rcs.tfm.api.model.TaoApiError;
import es.rcs.tfm.main.AppNames;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component(AppNames.SEC_AUTH_EX_EP)
public class RestExceptionAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {// implements AuthenticationEntryPoint {

	@Override
	public void commence(
			final HttpServletRequest request, 
			final HttpServletResponse response,
			final AuthenticationException authException) throws IOException { //, ServletException {

		String message = "No authentication exception provided. This is an unexpected error.";
		if (authException != null) {
			message = authException.getMessage();
		}

		TaoApiError error = new TaoApiError(
			HttpStatus.UNAUTHORIZED, 
			message,
			request.getRequestURI());

		final String data = error.toJSON();

		response.addHeader("WWW-Authenticate", "Basic realm=\"" + getRealmName() + "\"");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
		response.setContentType(APPLICATION_JSON_VALUE);
		response.setStatus(error.getStatus());
		response.setContentLength(data.length());
		response.getWriter().print(data);
		//response.getWriter().flush();
		
		//response.sendError(error.getStatus(), error.getMessage());
		
	}
	
	@Override
    public void afterPropertiesSet() throws Exception {
        setRealmName(AppNames.SEC_AUTH_REALM);
        super.afterPropertiesSet();
    }
	
}
