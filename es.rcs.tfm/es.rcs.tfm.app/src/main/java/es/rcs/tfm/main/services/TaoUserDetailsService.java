package es.rcs.tfm.main.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import es.rcs.tfm.db.DbNames;
import es.rcs.tfm.db.model.SecAuthorityEntity;
import es.rcs.tfm.db.model.SecGroupEntity;
import es.rcs.tfm.db.model.SecRoleEntity;
import es.rcs.tfm.db.model.SecUserEntity;
import es.rcs.tfm.db.repository.SecUserRepository;
import es.rcs.tfm.main.AppNames;

@Service( 
		AppNames.SEC_DETAILS_SERVICE )
@DependsOn ( {
		DbNames.DB_USR_REP } )
public class TaoUserDetailsService implements UserDetailsService {

	//http://javahotpot.blogspot.com.es/2013/12/spring-security-adding-more-information.html
	
	private static final Logger LOG = LoggerFactory.getLogger(TaoUserDetailsService.class);

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		SecUserEntity user = null;
		if (EmailValidator.getInstance().isValid(username)) {
			user = this.userRepository.findByEmail(username);
		} else {
			user = this.userRepository.findByUsername(username);
		}

		UserDetails details = null;
		if (user != null) {
			details = getDetails(user);
		} else {
			throw new UsernameNotFoundException("No user found with username " + username);
		}
		
		return details;

	}
	
	private UserDetails getDetails(SecUserEntity user) {
		
		if (user == null) {
			if (LOG.isInfoEnabled())
				LOG.info("Usuario no encontrado");
			throw new UsernameNotFoundException("Usuario no encontrado");
		} 
		
		Set<GrantedAuthority> setAuths = new HashSet<GrantedAuthority>();

		try {

			for (SecAuthorityEntity auth: user.getAuthorities()) {
				setAuths.add(new SimpleGrantedAuthority(auth.getCode()));
			}
			
			for (SecRoleEntity role : user.getRoles()) {
				for (SecAuthorityEntity auth: role.getAuthorities()) {
					setAuths.add(new SimpleGrantedAuthority(auth.getCode()));
				}
			}
			
			for (SecGroupEntity group : user.getGroups()) {
				for (SecAuthorityEntity auth: group.getAuthorities()) {
					setAuths.add(new SimpleGrantedAuthority(auth.getCode()));
				}
			}

		} catch (Exception ex) {
			if (LOG.isDebugEnabled())
				LOG.debug("Error al rcuperar los permisos");
		}

		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(setAuths);

		User userDetails = new User(
				user.getUsername(), 
				user.getPassword(), 
				user.isEnabled(), 
				!user.isExpired(), 
				!user.isPasswordExpired(), 
				!user.isLocked(), 
				authorities);

		return userDetails;
		
	}

	@Autowired
	@Qualifier(	DbNames.DB_USR_REP )
	private SecUserRepository userRepository;

}
