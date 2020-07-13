package es.rcs.tfm.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.StringPath;

import es.rcs.tfm.db.DbNames;
import es.rcs.tfm.db.model.QSecModuleEntity;
import es.rcs.tfm.db.model.SecFunctionEntity;
import es.rcs.tfm.db.model.SecModuleEntity;

@Repository(DbNames.DB_MOD_REP)
public interface SecModuleRepository extends 
		JpaRepository<SecModuleEntity, Long>,
		QuerydslPredicateExecutor<SecModuleEntity>, 
		QuerydslBinderCustomizer<QSecModuleEntity>{
	
	@Override
	default void customize(QuerydslBindings bindings, QSecModuleEntity root) {
	
		bindings
			.bind(String.class)
			.first((StringPath path, String value) -> path.containsIgnoreCase(value));
		
		bindings
			.excluding(
				root.id,
				root.uuid,
				root.lock,
				root.createdAt,
				root.createdBy,
				root.modifiedAt,
				root.modifiedBy);
		
	}

	public SecModuleEntity findByCode(
			@Param(SecModuleEntity.ATT_CODE) String code);

}
