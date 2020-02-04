package es.rcs.tfm.api.repository;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import es.rcs.tfm.api.ApiNames;
import es.rcs.tfm.db.model.SecFunctionEntity;
import es.rcs.tfm.db.model.SecModuleEntity;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.OneRelationshipRepositoryBase;
import io.crnk.core.repository.RelationshipMatcher;

@Repository(value = ApiNames.API_FUN_MOD_REP)
public class FunctionModuleRepository 
		extends OneRelationshipRepositoryBase<
				SecFunctionEntity, 
				Long, 
				SecModuleEntity, 
				Long>  {

	@Override
    public RelationshipMatcher getMatcher() {
		RelationshipMatcher matcher = new RelationshipMatcher();
		matcher
			.rule()
			.source(SecFunctionEntity.class)
			.target(SecModuleEntity.class)
			.add();
		return matcher;
    }

	@Override
	public Map<Long, SecModuleEntity> findOneRelations(
			Collection<Long> sourceIds, 
			String fieldName, 
			QuerySpec querySpec) {

		Map<Long, SecModuleEntity>result = new HashMap<>();

		for (Long id: sourceIds) {
			SecFunctionEntity item = repPri.findOne(id, querySpec);
			if (item != null) {
				SecModuleEntity data = repSec.findOne(item.getModuleId(), querySpec);
				if (data != null) result.put(id, data);
			}
		}

		return result;

	}

	@Autowired
	@Qualifier(value = ApiNames.API_FUN_REP)
	private FunctionRepository repPri;

	@Autowired
	@Qualifier(value = ApiNames.API_MOD_REP)
	private ModuleRepository repSec;

}
