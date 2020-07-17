package es.rcs.tfm.db.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.StringPath;

import es.rcs.tfm.db.DbNames;
import es.rcs.tfm.db.model.PubArticleEntity;
import es.rcs.tfm.db.model.PubValuesSubentity;
import es.rcs.tfm.db.model.QPubArticleEntity;

@Repository(DbNames.DB_ARTICLE_REP)
public interface PubArticleRepository extends 
		JpaRepository<PubArticleEntity, Long>,
		QuerydslPredicateExecutor<PubArticleEntity>, 
		QuerydslBinderCustomizer<QPubArticleEntity> {
	
	@Override
	default void customize(QuerydslBindings bindings, QPubArticleEntity root) {
	
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

	Optional<PubArticleEntity> findByPmid(String pmid);
	@Query(
			"SELECT a" + 
			" FROM " + 
			"  PubArticleEntity a JOIN " + 
			"  a.identifiers i" +
			" WHERE" +
			"  i.type = :type AND i.value = :value")
	List<PubArticleEntity> findByIdentifier(
			@Param(PubValuesSubentity.ATT_TYPE) String type, 
			@Param(PubValuesSubentity.ATT_VALUE) String value);

	@Query(
			"SELECT " + 
			"  a" + 
			" FROM " + 
			"  PubArticleEntity a JOIN " + 
			"  a.identifiers i" +
			" WHERE" +
			"  (i.type, i.value) IN (:ids)")
	List<PubArticleEntity> findByIdentifiers(
			@Param("ids") Map<String, String> ids);

}

