package es.rcs.tfm.db.model;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.crnk.core.queryspec.pagingspec.NumberSizePagingSpec;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiRelationId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
import io.crnk.core.resource.annotations.SerializeType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(
		callSuper = true)
@JsonApiResource(
		type = PubCenterEntity.RES_TYPE,
		resourcePath = PubCenterEntity.RES_ACTION,
		postable = false, patchable = false, deletable = false, 
		readable = true, sortable = true, filterable = true,
		pagingSpec = NumberSizePagingSpec.class )
@Table(
		name = PubCenterEntity.DB_TABLE,
		uniqueConstraints = {
			@UniqueConstraint(
					name = PubCenterEntity.DB_ID_PK, 
					columnNames = { PubCenterEntity.DB_ID }),
			@UniqueConstraint(
					name = PubCenterEntity.DB_UID_UK, 
					columnNames = { PubCenterEntity.DB_UID }),
			@UniqueConstraint(
					name = PubCenterEntity.DB_NAME_UK, 
					columnNames = { PubCenterEntity.DB_NAME })  })
@Entity
@Audited
@EntityListeners(
		value = AuditingEntityListener.class)
public class PubCenterEntity extends AuditedBaseEntity {

	@Transient
	private static final Logger LOG = LoggerFactory.getLogger(PubFileEntity.class);

	public static final String RES_ACTION					= "centres";
	public static final String RES_TYPE						= "Centre";

	public static final String RES_CENTRETYPE				= "type";
	public static final String RES_NAME						= "name";
	public static final String RES_IDENTIFIERS				= "identifiers";
	public static final String RES_AUTHOR_BOOK_CENTRE_IDS	= "authorBookCentreIds";
	public static final String RES_AUTHOR_BOOK_CENTRE		= "authorBookCentres";
	/*
  	public static final String RES_AUTHOR_IDS				= "authorIds";
	public static final String RES_AUTHORS					= "authors";
	public static final String RES_ARTICLE_IDS				= "articleIds";
	public static final String RES_ARTICLES					= "articles";
	 */
	
	public static final String DB_TABLE 					= "pub_centres";
	public static final String DB_ID_PK 					= "pub_cen_pk";
	public static final String DB_UID_UK					= "pub_cen_uid_uk";
	public static final String DB_NAME_UK					= "pub_cen_txt_uk";

	public static final String DB_CENTRETYPE				= "cen_typ";
	public static final String DB_NAME						= "cen_txt";

	public static final String DB_TABLE_IDS 				= "pub_centre_identifiers";
	public static final String DB_TABLE_IDS_PK				= "pub_cen_ids_pk";
	public static final String DB_TABLE_IDS_FK				= "pub_cen_ids_fk";
	public static final String DB_TABLE_IDS_UK				= "pub_cen_ids_uk";
	public static final String DB_TABLE_IDS_IDX			= "pub_cen_ids_id_idx";

	public static final String DB_CENTRE_ID					= "cen_id";

	public static final String ATT_NAME						= "name";
	public static final String ATT_IDENTIFIERS_IDS			= "identifierIds";
	public static final String ATT_IDENTIFIERS				= "identifiers";
	public static final String ATT_AUTHOR_BOOK_CENTRE_IDS	= "authorBookCentreIds";
	public static final String ATT_AUTHOR_BOOK_CENTRE		= "authorBookCentres";

	/*
	public static final String ATT_AUTHOR_IDS		= "authorIds";
	public static final String ATT_AUTHORS			= "authors";
	public static final String ATT_ARTICLE_IDS		= "articleIds";
	public static final String ATT_ARTICLES			= "articles";
	 */
	
	@JsonProperty(
			value = RES_CENTRETYPE,
			required = false)
	@Column(
			name = DB_CENTRETYPE, 
			unique = false,
			nullable = true, 
			length = 256)
	@Size(
			max = 256, 
			message = "El tipo de centro no puede sobrepasar los {max} caracteres.")
	public String centretype;

	
	@JsonProperty(
			value = RES_NAME,
			required = true)
	@Column(
			name = DB_NAME, 
			unique = true,
			nullable = false, 
			length = 2048)
	@NotNull(
			message = "El nombre no puede ser nulo")
	@Size(
			max = 2048, 
			message = "El nombre no puede sobrepasar los {max} caracteres.")
	private String name;


	@JsonApiRelation(
			serialize = SerializeType.EAGER,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
	@JsonProperty(
			value = RES_IDENTIFIERS,
			required = false)
	@CollectionTable(
			name = DB_TABLE_IDS, 
			joinColumns = @JoinColumn(
					name = DB_CENTRE_ID,
					referencedColumnName = DB_ID,
					foreignKey = @ForeignKey(
							value = ConstraintMode.NO_CONSTRAINT,
							name = DB_TABLE_IDS_FK)),
			uniqueConstraints = {
					@UniqueConstraint(
							name = DB_TABLE_IDS_PK, 
							columnNames = { DB_CENTRE_ID, PubValuesSubentity.DB_TYPE }),
					@UniqueConstraint(
							name = DB_TABLE_IDS_UK, 
							columnNames = { PubValuesSubentity.DB_TYPE, PubValuesSubentity.DB_VALUE }) },
			indexes = {
					@Index(
							unique = true,
							name = DB_TABLE_IDS_IDX,
							columnList = PubValuesSubentity.DB_VALUE + ", " + PubValuesSubentity.DB_TYPE) })
	@ElementCollection(
			fetch = FetchType.EAGER)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Set<PubValuesSubentity> identifiers;


	@JsonApiRelationId
	@JsonProperty(
			value = RES_AUTHOR_BOOK_CENTRE_IDS)
	@Transient
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Set<Long> authorBookCentreIds = null;


	@JsonApiRelation(
			idField = ATT_AUTHOR_BOOK_CENTRE_IDS, 
			mappedBy = PubArticleAuthorEntity.ATT_CENTRE,
			serialize = SerializeType.EAGER,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
	@JsonProperty(
			value = RES_AUTHOR_BOOK_CENTRE,
			required = false)
	@OneToMany(
			mappedBy = PubArticleAuthorEntity.ATT_CENTRE,
			fetch = FetchType.LAZY,
			cascade = { CascadeType.ALL })
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<PubArticleAuthorEntity> authorBookCentres;

	
	// CONSTRUCTOR -------------------------------------------------------------------------------------------

	public void setAuthorBookCentres(final Set<PubArticleAuthorEntity> items) {
		if ((items != null) && !items.isEmpty()) {
			this.authorBookCentres = items;
			Set<Long> itemIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
			if ((itemIds != null) && !itemIds.isEmpty()) {
				this.authorBookCentreIds = itemIds;
			}
		}
	}

	public boolean mergeIdentifiers(Set<PubValuesSubentity> items) {
		AtomicBoolean result = new AtomicBoolean(false);
		if ((items != null) && !items.isEmpty()) {
			if ((this.identifiers != null) && (!this.identifiers.isEmpty())) {
				items.forEach( item -> {
					if (!this.identifiers.contains(item)) {
						result.set(true);
						this.identifiers.add(item);
					}
				});
			} else {
				this.identifiers = items;
			}
		}
		return result.get();
	}

}
