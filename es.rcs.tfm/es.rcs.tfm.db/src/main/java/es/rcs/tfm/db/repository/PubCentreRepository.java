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
import es.rcs.tfm.db.model.PubCenterEntity;
import es.rcs.tfm.db.model.PubValuesSubentity;
import es.rcs.tfm.db.model.QPubCenterEntity;

@Repository(DbNames.DB_CENTRE_REP)
public interface PubCentreRepository extends 
		JpaRepository<PubCenterEntity, Long>,
		QuerydslPredicateExecutor<PubCenterEntity>, 
		QuerydslBinderCustomizer<QPubCenterEntity> {

	@Override
	default void customize(QuerydslBindings bindings, QPubCenterEntity root) {
		
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


	public Optional<PubCenterEntity> findByName(
			@Param(PubCenterEntity.ATT_NAME) String name);

	@Query(
			"SELECT a" + 
			" FROM " + 
			"  PubCenterEntity a JOIN " + 
			"  a.identifiers i" +
			" WHERE" +
			"  i.type = :type AND i.value = :value")
	List<PubCenterEntity> findByIdentifier(
			@Param(PubValuesSubentity.ATT_TYPE) String type, 
			@Param(PubValuesSubentity.ATT_VALUE) String value);

	@Query(
			"SELECT " + 
			"  a" + 
			" FROM " + 
			"  PubCenterEntity a JOIN " + 
			"  a.identifiers i" +
			" WHERE" +
			"  (i.type, i.value) IN (:ids)")
	List<PubCenterEntity> findByIdentifiers(
			@Param("ids") Map<String, String> ids);
	
}
