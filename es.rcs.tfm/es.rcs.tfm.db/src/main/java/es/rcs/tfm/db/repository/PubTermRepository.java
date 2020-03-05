package es.rcs.tfm.db.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.StringPath;

import es.rcs.tfm.db.DbNames;
import es.rcs.tfm.db.model.PubTermEntity;
import es.rcs.tfm.db.model.QPubTermEntity;

@Repository(DbNames.DB_TERM_REP)
public interface PubTermRepository extends 
		JpaRepository<PubTermEntity, Long>,
		QuerydslPredicateExecutor<PubTermEntity>, 
		QuerydslBinderCustomizer<QPubTermEntity> {

	@Override
	default void customize(QuerydslBindings bindings, QPubTermEntity root) {
		
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

	Optional<PubTermEntity> findByProviderAndDesctypeAndCode(
			@Param("provider") String provider, 
			@Param("desctype") String desctype, 
			@Param("code") String code);

	
}
