package es.rcs.tfm.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.StringPath;

import es.rcs.tfm.db.DbNames;
import es.rcs.tfm.db.model.PubArticlePublicationEntity;
import es.rcs.tfm.db.model.PubArticlePublicationKey;
import es.rcs.tfm.db.model.QPubArticlePublicationEntity;

@Repository(DbNames.DB_ARTICLE_PUBLI_REP)
public interface PubArticlePublicationRepository extends 
		JpaRepository<PubArticlePublicationEntity, PubArticlePublicationKey>,
		QuerydslPredicateExecutor<PubArticlePublicationEntity>, 
		QuerydslBinderCustomizer<QPubArticlePublicationEntity> {
		
	@Override
	default void customize(QuerydslBindings bindings, QPubArticlePublicationEntity root) {
	
	bindings
		.bind(String.class)
		.first((StringPath path, String value) -> path.containsIgnoreCase(value));
	
	bindings
		.excluding(
			root.id);
	
	}

}