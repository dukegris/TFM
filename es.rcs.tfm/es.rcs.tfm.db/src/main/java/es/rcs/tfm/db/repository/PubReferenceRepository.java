package es.rcs.tfm.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.StringPath;

import es.rcs.tfm.db.DbNames;
import es.rcs.tfm.db.model.PubReferenceSubentity;
import es.rcs.tfm.db.model.QPubReferenceSubentity;

@Repository(DbNames.DB_REFERENCE_REP)
public interface PubReferenceRepository extends 
		JpaRepository<PubReferenceSubentity, Long>,
		QuerydslPredicateExecutor<PubReferenceSubentity>, 
		QuerydslBinderCustomizer<QPubReferenceSubentity> {

	@Override
	default void customize(QuerydslBindings bindings, QPubReferenceSubentity root) {
		
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
	
}
