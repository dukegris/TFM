package es.rcs.tfm.db.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.StringPath;

import es.rcs.tfm.db.DbNames;
import es.rcs.tfm.db.model.PubArticleAuthorEntity;
import es.rcs.tfm.db.model.PubArticleAuthorKey;
import es.rcs.tfm.db.model.QPubArticleAuthorEntity;

@Repository(DbNames.DB_ARTICLE_AUTHOR_REP)
public interface PubArticleAuthorRepository extends 
		JpaRepository<PubArticleAuthorEntity, PubArticleAuthorKey>,
		QuerydslPredicateExecutor<PubArticleAuthorEntity>, 
		QuerydslBinderCustomizer<QPubArticleAuthorEntity> {
		
	@Override
	default void customize(QuerydslBindings bindings, QPubArticleAuthorEntity root) {
	
	bindings
		.bind(String.class)
		.first((StringPath path, String value) -> path.containsIgnoreCase(value));
	
	bindings
		.excluding(
			root.id);
	
	}

	Optional<PubArticleAuthorEntity> findByKey(PubArticleAuthorKey id);

	/*
	@Query( value =
			"SELECT a" + 
			" FROM PubArticleAuthorEntity a " +
			" WHERE" +
			" i.type = :type AND i.value = :value")
	List<PubPublicationEntity> findByIdentifier(
			@Param("type") String type, 
			@Param("value") String value);
	Optional<PubArticleAuthorEntity> findByIds(PubArticleAuthorKey id);
	 */
}