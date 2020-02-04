package es.rcs.tfm.api.repository;

import org.springframework.stereotype.Repository;

import es.rcs.tfm.api.ApiNames;
import es.rcs.tfm.db.model.SecFunctionEntity;
import io.crnk.data.jpa.JpaEntityRepositoryBase;

@Repository(value = ApiNames.API_FUN_REP)
public class FunctionRepository extends JpaEntityRepositoryBase<SecFunctionEntity, Long> {

	public FunctionRepository() {
		super(SecFunctionEntity.class);
	}

}
