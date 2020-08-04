package es.rcs.tfm.main.config;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import es.rcs.tfm.db.DbNames;
import es.rcs.tfm.main.AppNames;

@EnableJpaAuditing(
		auditorAwareRef = DbNames.DB_AUDIT_PROVIDER,
		dateTimeProviderRef = DbNames.DB_DATE_PROVIDER)
@Configuration(
		AppNames.BBDD_AUDIT_CONFIG )
public class AuditConfig {

	@Bean(name = DbNames.DB_DATE_PROVIDER)
	public DateTimeProvider dateTimeProvider() {
		return () -> Optional.of(ZonedDateTime.now());
	}
	
	@Bean( DbNames.DB_AUDIT_PROVIDER )
	public AuditorAware<String> auditorProvider() {
		return () -> Optional.ofNullable(getUsernameOfAuthenticatedUser());
	}
	
	public static String getUsernameOfAuthenticatedUser() {
    	
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
 
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymousUser";
        }
 
        String username = "";
        Object principal = authentication.getPrincipal();
        if (principal.getClass().equals(String.class)) {
        	username = (String)principal;
        //} else if (principal.getClass().equals(TaoDetails.class)) {
        //	username = ((TaoDetails)principal).getUsername();
    	//} else if (principal instanceof LdapUserDetails) {
        //    username = authentication.getName();
        } else if (principal.getClass().equals(UserDetails.class)) {
            username = ((UserDetails)principal).getUsername();
        } else if (principal.getClass().equals(User.class)) {
            username = ((User)principal).getUsername();
        } else {
        	username = principal.toString();
        }
		 
		return username;
	
	}

}