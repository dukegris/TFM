package es.rcs.tfm.main.services;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Service;

import es.rcs.tfm.db.DbNames;
import es.rcs.tfm.db.model.SecTokenEntity;
import es.rcs.tfm.db.model.SecUserEntity;
import es.rcs.tfm.db.repository.SecTokenRepository;
import es.rcs.tfm.db.repository.SecUserRepository;
import es.rcs.tfm.main.AppNames;

@Service( 
		AppNames.SEC_REMEMBERME_SERVICE )
@DependsOn ( {
		DbNames.DB_USR_REP,
		DbNames.DB_TKN_REP } )
public class TaoRememberMeService implements PersistentTokenRepository {

	@Override
	public void createNewToken(PersistentRememberMeToken token) {

		SecUserEntity user = null;
		SecTokenEntity userToken = null;
		
		user = userRepository.findByUsername(token.getUsername());
		userToken = new SecTokenEntity(user, token.getSeries(), token.getTokenValue(), token.getDate());
		tokenRepository.save(userToken);
        	
	}

	@Override
	public void updateToken(String series, String tokenValue, Date lastUsed) {
		
		SecTokenEntity userToken = null;
		
		userToken = tokenRepository.findBySerie(series);

        if (userToken != null){
        	
        	userToken.setToken(tokenValue);
        	userToken.setLastUsed(lastUsed);

    		tokenRepository.save(userToken);

        }
        
	}

	@Override
	public PersistentRememberMeToken getTokenForSeries(String seriesId) {

		SecTokenEntity userToken = null;
		
		userToken = tokenRepository.findBySerie(seriesId);

		if (userToken != null) {
			return new PersistentRememberMeToken(
					userToken.getUser().getUsername(), 
					userToken.getSerie(), 
					userToken.getToken(), 
					userToken.getLastUsed());
		}
		return null;

	}

	@Override
	public void removeUserTokens(String username) {

		tokenRepository.deleteByUsername(username);
		
	}

	@Autowired 
	@Qualifier(	DbNames.DB_USR_REP )
	private SecUserRepository userRepository;

	@Autowired
	@Qualifier(	DbNames.DB_TKN_REP )
	private SecTokenRepository tokenRepository;

}