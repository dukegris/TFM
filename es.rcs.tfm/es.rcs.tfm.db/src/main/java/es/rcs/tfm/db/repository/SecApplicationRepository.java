package es.rcs.tfm.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.StringPath;

import es.rcs.tfm.db.DbNames;
import es.rcs.tfm.db.model.QSecApplicationEntity;
import es.rcs.tfm.db.model.SecApplicationEntity;

@Repository(DbNames.DB_APP_REP)
public interface SecApplicationRepository extends 
		JpaRepository<SecApplicationEntity, Long>,
		QuerydslPredicateExecutor<SecApplicationEntity>, 
		QuerydslBinderCustomizer<QSecApplicationEntity>{
	
	@Override
	default void customize(QuerydslBindings bindings, QSecApplicationEntity root) {
	
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

	public SecApplicationEntity findByCode(
			@Param(SecApplicationEntity.ATT_CODE) String code);

}
