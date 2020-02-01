package es.rcs.tfm.api.repository;

import org.springframework.stereotype.Component;

import es.rcs.tfm.db.model.SecApplicationEntity;
import es.rcs.tfm.db.model.SecAuthorityEntity;
import io.crnk.data.jpa.JpaEntityRepositoryBase;

@Component
public class RoleRepository extends JpaEntityRepositoryBase<SecAuthorityEntity, Long> {

	public RoleRepository() {
		super(SecAuthorityEntity.class);
	}

}
