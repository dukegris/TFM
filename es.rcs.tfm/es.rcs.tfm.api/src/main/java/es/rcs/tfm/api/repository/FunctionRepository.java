package es.rcs.tfm.api.repository;

import org.springframework.stereotype.Component;

import es.rcs.tfm.db.model.SecFunctionEntity;
import io.crnk.data.jpa.JpaEntityRepositoryBase;

@Component
public class FunctionRepository extends JpaEntityRepositoryBase<SecFunctionEntity, Long> {

	public FunctionRepository() {
		super(SecFunctionEntity.class);
	}

}
