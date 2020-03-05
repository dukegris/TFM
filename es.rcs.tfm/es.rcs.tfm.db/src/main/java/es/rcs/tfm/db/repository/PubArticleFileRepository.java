package es.rcs.tfm.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.StringPath;

import es.rcs.tfm.db.DbNames;
import es.rcs.tfm.db.model.PubArticleFileEntity;
import es.rcs.tfm.db.model.PubArticleFileKey;
import es.rcs.tfm.db.model.QPubArticleFileEntity;

@Repository(DbNames.DB_ARTICLE_FILE_REP)
public interface PubArticleFileRepository extends 
		JpaRepository<PubArticleFileEntity, PubArticleFileKey>,
		QuerydslPredicateExecutor<PubArticleFileEntity>, 
		QuerydslBinderCustomizer<QPubArticleFileEntity> {
		
	@Override
	default void customize(QuerydslBindings bindings, QPubArticleFileEntity root) {
	
	bindings
		.bind(String.class)
		.first((StringPath path, String value) -> path.containsIgnoreCase(value));
	
	bindings
		.excluding(
			root.id);
	
	}

}