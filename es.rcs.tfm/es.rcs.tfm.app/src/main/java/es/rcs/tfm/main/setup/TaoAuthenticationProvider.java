package es.rcs.tfm.main.setup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class TaoAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider { 

	private static final Logger LOG = LoggerFactory.getLogger(TaoAuthenticationProvider.class);

	public PasswordEncoder passwordEncoder;
	private UserDetailsService userDetailsService;
	protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

	private static final String USER_NOT_FOUND_PASSWORD = "userNotFoundPassword";
	private String userNotFoundEncodedPassword;

	public TaoAuthenticationProvider(
			UserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) {
		
		super();

		this.userNotFoundEncodedPassword = passwordEncoder.encode(USER_NOT_FOUND_PASSWORD);
		this.passwordEncoder = passwordEncoder;
		this.userDetailsService = userDetailsService;
		
	}

	@Override
	protected void additionalAuthenticationChecks(
			UserDetails userDetails,
			UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

		if (authentication.getCredentials() == null) {
			LOG.debug("Authentication failed: no credentials provided");

			throw new BadCredentialsException(messages.getMessage(
					"AbstractUserDetailsAuthenticationProvider.badCredentials",
					"Bad credentials"));
		}

		String presentedPassword = authentication.getCredentials().toString();
		if (!this.passwordEncoder.matches(presentedPassword, userDetails.getPassword())) {
			LOG.debug("Authentication failed: password does not match stored value");

			throw new BadCredentialsException(messages.getMessage(
					"AbstractUserDetailsAuthenticationProvider.badCredentials",
					"Bad credentials"));
		}

		
	}

	@Override
	protected UserDetails retrieveUser(
			String username, 
			UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {

		UserDetails loadedUser = null;

		try {
			
			loadedUser = userDetailsService.loadUserByUsername(username);

		} catch (UsernameNotFoundException notFound) {
			// Se introduce un código para asimilar el tiempo de los usuarios no encotrados
			// Los encontrados luego hacen el chequeo additionalAuthenticationChecks
			if (authentication.getCredentials() != null) {
				String presentedPassword = authentication.getCredentials().toString();
				passwordEncoder.matches(userNotFoundEncodedPassword, presentedPassword);
			}
			throw notFound;
		} catch (Exception repositoryProblem) {
			throw new InternalAuthenticationServiceException(
					repositoryProblem.getMessage(), repositoryProblem);
		}

		if (loadedUser == null) {
			throw new InternalAuthenticationServiceException(
					"UserDetailsService returned null, which is an interface contract violation");
		}

		return loadedUser;

	}

}
