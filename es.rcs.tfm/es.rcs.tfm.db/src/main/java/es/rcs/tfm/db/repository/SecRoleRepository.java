package es.rcs.tfm.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.StringPath;

import es.rcs.tfm.db.DbNames;
import es.rcs.tfm.db.model.QSecRoleEntity;
import es.rcs.tfm.db.model.SecRoleEntity;

@Repository(DbNames.DB_ROL_REP)
public interface SecRoleRepository extends 
		JpaRepository<SecRoleEntity, Long>,
		QuerydslPredicateExecutor<SecRoleEntity>, 
		QuerydslBinderCustomizer<QSecRoleEntity>{
	
	@Override
	default void customize(QuerydslBindings bindings, QSecRoleEntity root) {
	
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

	public SecRoleEntity findByCode(
			@Param(SecRoleEntity.ATT_CODE) String code);

}
