package es.rcs.tfm.api.repository;

import org.springframework.stereotype.Component;

import es.rcs.tfm.db.model.SecApplicationEntity;
import es.rcs.tfm.db.model.SecAuthorityEntity;
import es.rcs.tfm.db.model.SecRoleEntity;
import io.crnk.data.jpa.JpaEntityRepositoryBase;

@Component
public class AuthorityRepository extends JpaEntityRepositoryBase<SecRoleEntity, Long> {

	public AuthorityRepository() {
		super(SecRoleEntity.class);
	}

}
