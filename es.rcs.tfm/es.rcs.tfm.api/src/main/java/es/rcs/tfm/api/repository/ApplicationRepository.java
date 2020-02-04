package es.rcs.tfm.api.repository;

import org.springframework.stereotype.Repository;

import es.rcs.tfm.api.ApiNames;
import es.rcs.tfm.db.model.SecApplicationEntity;
import io.crnk.data.jpa.JpaEntityRepositoryBase;

@Repository(value = ApiNames.API_APP_REP)
public class ApplicationRepository extends JpaEntityRepositoryBase<SecApplicationEntity, Long> {

	public ApplicationRepository() {
		super(SecApplicationEntity.class);
	}

}
