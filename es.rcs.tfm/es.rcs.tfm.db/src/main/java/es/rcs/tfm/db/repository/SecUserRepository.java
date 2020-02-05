package es.rcs.tfm.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.StringPath;

import es.rcs.tfm.db.DbNames;
import es.rcs.tfm.db.model.QSecUserEntity;
import es.rcs.tfm.db.model.SecUserEntity;

@Repository(DbNames.DB_USR_REP)
public interface SecUserRepository extends 
		JpaRepository<SecUserEntity, Long>,
		QuerydslPredicateExecutor<SecUserEntity>, 
		QuerydslBinderCustomizer<QSecUserEntity>{
	
	@Override
	default void customize(QuerydslBindings bindings, QSecUserEntity root) {
	
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

	SecUserEntity findByName(
			@Param("name") String name);
	SecUserEntity findByEmail(
			@Param("email") String email);

}
