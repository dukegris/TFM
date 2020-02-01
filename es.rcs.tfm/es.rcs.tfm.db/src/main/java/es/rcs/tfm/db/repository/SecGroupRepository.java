package es.rcs.tfm.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.StringPath;

import es.rcs.tfm.db.DbNames;
import es.rcs.tfm.db.model.QSecGroupEntity;
import es.rcs.tfm.db.model.SecGroupEntity;

@Repository(DbNames.DB_GRP_REP)
public interface SecGroupRepository extends 
		JpaRepository<SecGroupEntity, Long>,
		QuerydslPredicateExecutor<SecGroupEntity>, 
		QuerydslBinderCustomizer<QSecGroupEntity>{
	
	@Override
	default void customize(QuerydslBindings bindings, QSecGroupEntity root) {
	
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

	public SecGroupEntity findByCode(
			@Param("code") String code);

}
