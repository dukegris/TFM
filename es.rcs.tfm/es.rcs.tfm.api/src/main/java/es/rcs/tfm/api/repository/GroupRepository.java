package es.rcs.tfm.api.repository;

import org.springframework.stereotype.Component;

import es.rcs.tfm.db.model.SecGroupEntity;
import io.crnk.data.jpa.JpaEntityRepositoryBase;

@Component
public class GroupRepository extends JpaEntityRepositoryBase<SecGroupEntity, Long> {

	public GroupRepository() {
		super(SecGroupEntity.class);
	}

}
