package es.rcs.tfm.db.repository;

import java.util.List;
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
import es.rcs.tfm.db.model.PubPublicationEntity;
import es.rcs.tfm.db.model.QPubPublicationEntity;

@Repository(DbNames.DB_PUBLICATION_REP)
public interface PubPublicationRepository extends 
		JpaRepository<PubPublicationEntity, Long>,
		QuerydslPredicateExecutor<PubPublicationEntity>, 
		QuerydslBinderCustomizer<QPubPublicationEntity> {

	@Override
	default void customize(QuerydslBindings bindings, QPubPublicationEntity root) {
		
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

	/*
	@Query(
			"SELECT a" + 
			" FROM PubArticleEntity a" +
			" WHERE" +
			" KEY(a.identifiers) = :type AND VALUE(a.identifiers) = :value")
			*/
	@Query( value =
			"SELECT a" + 
			" FROM PubPublicationEntity a JOIN a.identifiers i" +
			" WHERE" +
			" i.type = :type AND i.value = :value")
	List<PubPublicationEntity> findByIdentifier(
			@Param("type") String type, 
			@Param("value") String value);

	Optional<PubPublicationEntity> findByTitle(String title);

	Optional<PubPublicationEntity> findByTypeAndTitle(String type, String title);
	
}
