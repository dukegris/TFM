package es.rcs.tfm.api.repository;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import es.rcs.tfm.api.ApiNames;
import es.rcs.tfm.db.model.SecApplicationEntity;
import es.rcs.tfm.db.model.SecModuleEntity;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.OneRelationshipRepositoryBase;
import io.crnk.core.repository.RelationshipMatcher;

@Repository(value = ApiNames.API_MOD_APP_REP)
public class ModuleApplicationRepository extends OneRelationshipRepositoryBase<SecModuleEntity, Long, SecApplicationEntity, Long> {

	@Override
    public RelationshipMatcher getMatcher() {
		RelationshipMatcher matcher = new RelationshipMatcher();
		matcher
			.rule()
			.source(SecModuleEntity.class)
			.target(SecApplicationEntity.class)
			.add();
		return matcher;
    }

	@Override
	public Map<Long, SecApplicationEntity> findOneRelations(
			Collection<Long> sourceIds, 
			String fieldName, 
			QuerySpec querySpec) {

		Map<Long, SecApplicationEntity>result = new HashMap<>();

		for (Long id: sourceIds) {
			SecModuleEntity item = repPri.findOne(id, querySpec);
			if (item != null) {
				SecApplicationEntity data = repSec.findOne(item.getApplicationId(), querySpec);
				if (data != null) result.put(id, data);
			}
		}

		return result;

	}

	@Autowired
	@Qualifier(value = ApiNames.API_MOD_REP)
	private ModuleRepository repPri;

	@Autowired
	@Qualifier(value = ApiNames.API_APP_REP)
	private ApplicationRepository repSec;

}
