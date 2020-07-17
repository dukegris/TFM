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
import es.rcs.tfm.db.model.PubPublicationEntity;
import es.rcs.tfm.db.model.PubValuesSubentity;
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

	Optional<PubPublicationEntity> findByTitle(
			@Param(PubPublicationEntity.ATT_TITLE) String title);

	Optional<PubPublicationEntity> findByTypeAndTitle(
			@Param(PubPublicationEntity.ATT_PUBLICATIONTYPE) String type,
			@Param(PubPublicationEntity.ATT_TITLE) String title);

	@Query( value =
			"SELECT a" + 
			" FROM " + 
			"  PubPublicationEntity a JOIN " + 
			"  a.identifiers i" +
			" WHERE" +
			" i.type = :type AND i.value = :value")
	List<PubPublicationEntity> findByIdentifier(
			@Param(PubValuesSubentity.ATT_TYPE) String type, 
			@Param(PubValuesSubentity.ATT_VALUE) String value);

	@Query(
			"SELECT " + 
			"  a" + 
			" FROM " + 
			"  PubPublicationEntity a JOIN " + 
			"  a.identifiers i" +
			" WHERE" +
			"  (i.type, i.value) IN (:ids)")
	List<PubPublicationEntity> findByIdentifiers(
			@Param("ids") Map<String, String> ids);
	
}
