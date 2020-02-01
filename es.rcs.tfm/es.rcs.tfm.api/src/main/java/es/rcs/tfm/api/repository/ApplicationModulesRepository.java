package es.rcs.tfm.api.repository;

import java.util.Collection;
import java.util.Map;

import es.rcs.tfm.db.model.SecFunctionEntity;
import es.rcs.tfm.db.model.SecModuleEntity;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.OneRelationshipRepositoryBase;
import io.crnk.core.repository.RelationshipMatcher;

public class ApplicationModulesRepository extends OneRelationshipRepositoryBase {

	@Override
    public RelationshipMatcher getMatcher() {
		RelationshipMatcher matcher = new RelationshipMatcher();
		matcher
			.rule()
			.source(SecModuleEntity.class)
			.target(SecFunctionEntity.class)
			.add();
		return matcher;
    }

	@Override
	public Map findOneRelations(
			Collection sourceIds, 
			String fieldName, 
			QuerySpec querySpec) {
		return null;
	}

}
