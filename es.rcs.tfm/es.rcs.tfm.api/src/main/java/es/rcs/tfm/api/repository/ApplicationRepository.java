package es.rcs.tfm.api.repository;

import org.springframework.stereotype.Component;

import es.rcs.tfm.db.model.SecApplicationEntity;
import io.crnk.data.jpa.JpaEntityRepositoryBase;

@Component
public class ApplicationRepository extends JpaEntityRepositoryBase<SecApplicationEntity, Long> {

	public ApplicationRepository() {
		super(SecApplicationEntity.class);
	}

}
