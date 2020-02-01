package es.rcs.tfm.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.StringPath;

import es.rcs.tfm.db.DbNames;
import es.rcs.tfm.db.model.QSecTokenEntity;
import es.rcs.tfm.db.model.SecTokenEntity;

@Repository(DbNames.DB_TKN_REP)
public interface SecTokenRepository extends 
		JpaRepository<SecTokenEntity, Long>,
		QuerydslPredicateExecutor<SecTokenEntity>, 
		QuerydslBinderCustomizer<QSecTokenEntity>{
	
	@Override
	default void customize(QuerydslBindings bindings, QSecTokenEntity root) {
	
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

	SecTokenEntity findBySerie(
			@Param("serie") String serie);

	@Modifying
	@Query(value="delete from SecTokenEntity t where t.user in (select u from SecUserEntity u where u.username = ?1)")
	void deleteByUsername(String username);

}
