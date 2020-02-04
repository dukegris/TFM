package es.rcs.tfm.api.repository;

import org.springframework.stereotype.Repository;

import es.rcs.tfm.api.ApiNames;
import es.rcs.tfm.db.model.SecModuleEntity;
import io.crnk.data.jpa.JpaEntityRepositoryBase;

@Repository(value = ApiNames.API_MOD_REP)
public class ModuleRepository extends JpaEntityRepositoryBase<SecModuleEntity, Long> {

	public ModuleRepository() {
		super(SecModuleEntity.class);
	}

}
