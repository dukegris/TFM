package es.rcs.tfm.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.StringPath;

import es.rcs.tfm.db.DbNames;
import es.rcs.tfm.db.model.PubArticleTermEntity;
import es.rcs.tfm.db.model.PubArticleTermKey;
import es.rcs.tfm.db.model.QPubArticleTermEntity;

@Repository(DbNames.DB_ARTICLE_TERM_REP)
public interface PubArticleTermRepository extends 
		JpaRepository<PubArticleTermEntity, PubArticleTermKey>,
		QuerydslPredicateExecutor<PubArticleTermEntity>, 
		QuerydslBinderCustomizer<QPubArticleTermEntity> {
		
	@Override
	default void customize(QuerydslBindings bindings, QPubArticleTermEntity root) {
	
	bindings
		.bind(String.class)
		.first((StringPath path, String value) -> path.containsIgnoreCase(value));
	
	bindings
		.excluding(
			root.id);
	
	}

}