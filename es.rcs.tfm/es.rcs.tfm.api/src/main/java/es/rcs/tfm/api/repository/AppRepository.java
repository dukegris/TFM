package es.rcs.tfm.api.repository;

import org.springframework.stereotype.Component;

import es.rcs.tfm.db.model.SecAppEntity;
import io.crnk.data.jpa.JpaEntityRepositoryBase;

@Component
public class AppRepository extends JpaEntityRepositoryBase<SecAppEntity, Long> {

	public AppRepository() {
		super(SecAppEntity.class);
	}

}
