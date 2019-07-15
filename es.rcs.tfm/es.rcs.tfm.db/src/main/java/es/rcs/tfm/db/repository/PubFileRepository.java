package es.rcs.tfm.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.StringPath;

import es.rcs.tfm.db.DbNames;
import es.rcs.tfm.db.model.PubFileEntity;
import es.rcs.tfm.db.model.QPubFileEntity;

@Repository(DbNames.DB_FILE_REP)
public interface PubFileRepository extends 
		JpaRepository<PubFileEntity, Long>,
		QuerydslPredicateExecutor<PubFileEntity>, 
		QuerydslBinderCustomizer<QPubFileEntity> {

	@Override
	default void customize(QuerydslBindings bindings, QPubFileEntity root) {
		
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
	
	public PubFileEntity findBySourceAndFileName(
			@Param("source") String source,
			@Param("fileName") String fileName);

}
