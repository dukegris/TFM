package es.rcs.tfm.api.repository;

import org.springframework.stereotype.Component;

import es.rcs.tfm.db.model.SecModuleEntity;
import io.crnk.data.jpa.JpaEntityRepositoryBase;

@Component
public class ModuleRepository extends JpaEntityRepositoryBase<SecModuleEntity, Long> {

	public ModuleRepository() {
		super(SecModuleEntity.class);
	}

}
