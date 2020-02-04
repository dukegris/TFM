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
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ManyRelationshipRepositoryBase;
import io.crnk.core.repository.RelationshipMatcher;
import io.crnk.core.resource.list.ResourceList;

@Repository(value = ApiNames.API_APP_MOD_REP)
public class ApplicationModulesRepository
		extends ManyRelationshipRepositoryBase<
				SecApplicationEntity, 
				Long, 
				SecModuleEntity, 
				Long> {

	@Override
	public RelationshipMatcher getMatcher() {
		RelationshipMatcher matcher = new RelationshipMatcher();
		matcher.rule().source(SecApplicationEntity.class).target(SecModuleEntity.class).add();
		return matcher;
	}

	@Override
	public Map<Long, ResourceList<SecModuleEntity>> findManyRelations(
			Collection<Long> sourceIds, 
			String fieldName,
			QuerySpec querySpec) {

		Map<Long, ResourceList<SecModuleEntity>>result = new HashMap<>();

		for (Long id: sourceIds) {
			querySpec.addFilter(new FilterSpec(Arrays.asList("applicationId"), FilterOperator.EQ, id));
			ResourceList<SecModuleEntity> item = rep.findAll(querySpec);
			if (item != null) result.put(id, item);
		}

		return result;

	}

	@Autowired
	@Qualifier(value = ApiNames.API_MOD_REP)
	private ModuleRepository rep;

}
