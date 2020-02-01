package es.rcs.tfm.api.repository;

import java.util.Collection;
import java.util.Map;

import es.rcs.tfm.db.model.SecApplicationEntity;
import es.rcs.tfm.db.model.SecModuleEntity;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.OneRelationshipRepositoryBase;
import io.crnk.core.repository.RelationshipMatcher;

public class ModuleFunctionsRepository extends OneRelationshipRepositoryBase {

	@Override
    public RelationshipMatcher getMatcher() {
		RelationshipMatcher matcher = new RelationshipMatcher();
		matcher
			.rule()
			.source(SecApplicationEntity.class)
			.target(SecModuleEntity.class)
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
