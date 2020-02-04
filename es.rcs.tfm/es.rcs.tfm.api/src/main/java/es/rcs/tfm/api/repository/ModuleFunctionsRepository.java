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
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ManyRelationshipRepositoryBase;
import io.crnk.core.repository.RelationshipMatcher;
import io.crnk.core.resource.list.ResourceList;

@Repository(value = ApiNames.API_MOD_FUN_REP)
public class ModuleFunctionsRepository
		extends ManyRelationshipRepositoryBase<
				SecModuleEntity, 
				Long, 
				SecFunctionEntity, 
				Long> {

	@Override
	public RelationshipMatcher getMatcher() {
		RelationshipMatcher matcher = new RelationshipMatcher();
		matcher.rule().source(SecModuleEntity.class).target(SecFunctionEntity.class).add();
		return matcher;
	}

	@Override
	public Map<Long, ResourceList<SecFunctionEntity>> findManyRelations(
			Collection<Long> sourceIds, 
			String fieldName, 
			QuerySpec querySpec) {

		Map<Long, ResourceList<SecFunctionEntity>>result = new HashMap<>();

		for (Long id: sourceIds) {
			querySpec.addFilter(new FilterSpec(Arrays.asList("moduleId"), FilterOperator.EQ, id));
			ResourceList<SecFunctionEntity> item = rep.findAll(querySpec);
			if (item != null) result.put(id, item);
		}

		return result;

	}

	@Autowired
	@Qualifier(value = ApiNames.API_FUN_REP)
	private FunctionRepository rep;

}
