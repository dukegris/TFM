package es.rcs.tfm.db.repository;

import java.util.List;

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
import es.rcs.tfm.db.model.QPubArticleEntity;

@Repository(DbNames.DB_ARTICLE_FILE_REP)
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

	@Query(
			"SELECT a" + 
			" FROM PubArticleEntity a" +
			" WHERE" +
			" KEY(a.identifiers) = :type AND VALUE(a.identifiers) = :value")
	List<PubArticleEntity> findByIdentificador(
			@Param("type") String type, 
			@Param("value") String value);
	
	PubArticleEntity findByPmid(String pmid);
	
}
