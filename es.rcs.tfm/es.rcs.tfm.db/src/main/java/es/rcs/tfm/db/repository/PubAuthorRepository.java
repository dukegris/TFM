package es.rcs.tfm.db.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.StringPath;

import es.rcs.tfm.db.DbNames;
import es.rcs.tfm.db.model.PubAuthorEntity;
import es.rcs.tfm.db.model.PubValuesSubentity;
import es.rcs.tfm.db.model.QPubAuthorEntity;

@Repository(DbNames.DB_AUTHOR_REP)
public interface PubAuthorRepository extends 
		JpaRepository<PubAuthorEntity, Long>,
		QuerydslPredicateExecutor<PubAuthorEntity>, 
		QuerydslBinderCustomizer<QPubAuthorEntity> {

	@Override
	default void customize(QuerydslBindings bindings, QPubAuthorEntity root) {
		
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

	@Query(
			"SELECT a" + 
			" FROM " + 
			"  PubAuthorEntity a JOIN " + 
			"  a.identifiers i" +
			" WHERE" +
			"  i.type = :type AND i.value = :value")
	List<PubAuthorEntity> findByIdentifier(
			@Param(PubValuesSubentity.ATT_TYPE) String type, 
			@Param(PubValuesSubentity.ATT_VALUE) String value);

	@Query(
			"SELECT a" + 
			" FROM PubAuthorEntity a JOIN " + 
			"  a.identifiers i" +
			" WHERE" +
			"  (i.type, i.value) IN (:ids)")
	List<PubAuthorEntity> findByIdentifiers(
			@Param("ids") Map<String, String> ids);

	
}
