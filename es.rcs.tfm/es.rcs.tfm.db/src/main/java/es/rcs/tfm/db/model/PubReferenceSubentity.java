package es.rcs.tfm.db.model;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

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
import javax.persistence.ManyToOne;
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
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
import io.crnk.core.resource.annotations.SerializeType;
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
		type = PubReferenceSubentity.RES_TYPE,
		resourcePath = PubReferenceSubentity.RES_ACTION,
		postable = false, patchable = false, deletable = false, 
		readable = true, sortable = true, filterable = true,
		pagingSpec = NumberSizePagingSpec.class )
@Table(
		name = PubReferenceSubentity.DB_TABLE,
		uniqueConstraints = {
			@UniqueConstraint(
					name = PubReferenceSubentity.DB_ID_PK, 
					columnNames = { PubReferenceSubentity.DB_ID })  })
@Entity
@Audited
@EntityListeners(
		value = AuditingEntityListener.class)
public class PubReferenceSubentity extends AuditedBaseEntity {

	@Transient
	private static final Logger LOG = LoggerFactory.getLogger(PubFileEntity.class);

	public static final String RES_ACTION					= "references";
	public static final String RES_TYPE						= "Reference";

	public static final String RES_ID						= "id";
	public static final String RES_REFERENCE				= "reference";
	public static final String RES_ARTICLE_ID				= "articleId";
	public static final String RES_ARTICLE					= "article";
	public static final String RES_IDENTIFIERS_IDS			= "identifierIds";
	public static final String RES_IDENTIFIERS				= "identifiers";
	
	public static final String DB_TABLE 					= "pub_references";
	public static final String DB_ID_PK 					= "pub_ref_pk";
	public static final String DB_ARTICLE_FK				= "pub_ref_art_fk";

	public static final String DB_REFERENCE					= "ref_txt";
	public static final String DB_ARTICLE_ID				= "art_id";

	public static final String DB_TABLE_IDS 				= "pub_reference_identifiers";
	public static final String DB_TABLE_IDS_PK				= "pub_ref_ids_pk";
	public static final String DB_TABLE_IDS_FK				= "pub_ref_ids_fk";
	public static final String DB_TABLE_IDS_UK				= "pub_ref_ids_uk";
	public static final String DB_TABLE_IDS_IDX			= "pub_ref_ids_id_idx";

	public static final String DB_REFERENCE_ID				= "ref_id";

	public static final String ATT_ARTICLE_ID				= "articleId";
	public static final String ATT_ARTICLE					= "article";
	public static final String ATT_IDENTIFIERS_IDS			= "identifierIds";
	public static final String ATT_IDENTIFIERS				= "identifiers";

	
	@JsonProperty(
			value = RES_REFERENCE,
			required = true)
	@Column(
			name = DB_REFERENCE, 
			unique = false,
			nullable = false, 
			length = 256)
	@NotNull(
			message = "La referencia no puede ser nula")
	@Size(
			max = 256, 
			message = "La referencia no puede sobrepasar los {max} caracteres.")
	private String reference;


	/*
	@JsonApiRelationId
	@JsonProperty(
			value = RES_ARTICLE_ID)
	@Column(
			name = DB_ARTICLE_ID,
			unique = false,
			nullable = true)
	@NotNull(
			message = "La aplicacion no puede ser nula")
	private Long articled;
	 */

	
	@JsonApiRelation(
//			idField = ATT_ARTICLE_ID,
			mappedBy = PubArticleEntity.ATT_REFERENCES,
			lookUp = LookupIncludeBehavior.NONE,
			serialize = SerializeType.ONLY_ID)
	@JsonProperty(
			value = RES_ARTICLE)
	@ManyToOne(
			fetch = FetchType.LAZY,
			cascade = {CascadeType.DETACH},
			optional = false)
	@JoinColumn(
			name = DB_ARTICLE_ID,
			referencedColumnName = PubArticleEntity.DB_ID,
			foreignKey = @ForeignKey (
					name = DB_ARTICLE_FK))
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	//@Setter(
	//		value = AccessLevel.NONE)
	private PubArticleEntity article;

	
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
					name = DB_REFERENCE_ID,
					referencedColumnName = DB_ID,
					foreignKey = @ForeignKey(
							value = ConstraintMode.NO_CONSTRAINT,
							name = DB_TABLE_IDS_FK)),
			uniqueConstraints = {
					@UniqueConstraint(
							name = DB_TABLE_IDS_PK, 
							columnNames = { DB_REFERENCE_ID, PubValuesSubentity.DB_TYPE }) },
			indexes = {
					@Index(
							unique = false,
							name = DB_TABLE_IDS_IDX,
							columnList = PubValuesSubentity.DB_VALUE + ", " + PubValuesSubentity.DB_TYPE) })
	@ElementCollection(
			fetch = FetchType.EAGER)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Set<PubValuesSubentity> identifiers;

	
	// CONSTRUCTOR -------------------------------------------------------------------------------------------

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
