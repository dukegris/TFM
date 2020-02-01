package es.rcs.tfm.api.repository;

import org.springframework.stereotype.Component;

import es.rcs.tfm.db.model.SecUserEntity;
import io.crnk.data.jpa.JpaEntityRepositoryBase;

@Component
public class UserRepository extends JpaEntityRepositoryBase<SecUserEntity, Long> {

	public UserRepository() {
		super(SecUserEntity.class);
	}

}
