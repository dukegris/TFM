package es.rcs.tfm.api.repository;

import org.springframework.stereotype.Component;

import es.rcs.tfm.db.model.SecTokenEntity;
import io.crnk.data.jpa.JpaEntityRepositoryBase;

@Component
public class TokenRepository extends JpaEntityRepositoryBase<SecTokenEntity, Long> {

	public TokenRepository() {
		super(SecTokenEntity.class);
	}

}
