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
		type = PubBlockSubentity.RES_TYPE,
		resourcePath = PubBlockSubentity.RES_ACTION,
		postable = false, patchable = false, deletable = false, 
		readable = true, sortable = true, filterable = true,
		pagingSpec = NumberSizePagingSpec.class )
@Table(
		name = PubBlockSubentity.DB_TABLE,
		uniqueConstraints = {
			@UniqueConstraint(
					name = PubBlockSubentity.DB_ID_PK, 
					columnNames = { PubBlockSubentity.DB_ID })  })
@Entity
@Audited
@EntityListeners(
		value = AuditingEntityListener.class)
public class PubBlockSubentity extends AuditedBaseEntity {

	@Transient
	private static final Logger LOG = LoggerFactory.getLogger(PubFileEntity.class);

	public static final String RES_ACTION					= "blocks";
	public static final String RES_TYPE						= "Block";

	public static final String RES_ID						= "id";
	public static final String RES_BLOCKTYPE				= "type";
	public static final String RES_OFFSET					= "offset";
	public static final String RES_TEXT						= "text";
	public static final String RES_ARTICLE_ID				= "articleId";
	public static final String RES_ARTICLE					= "article";
	public static final String RES_ANNOTATONS_IDS			= "annotationIds";
	public static final String RES_ANNOTATONS				= "annotations";
	
	public static final String DB_TABLE 					= "pub_blocks";
	public static final String DB_ID_PK 					= "pub_blk_pk";
	public static final String DB_ARTICLE_FK				= "pub_blk_art_fk";

	public static final String DB_BLOCKTYPE					= "blk_typ";
	public static final String DB_OFFSET					= "blk_off";
	public static final String DB_TEXT						= "blk_txt";
	public static final String DB_ARTICLE_ID				= "art_id";

	public static final String DB_TABLE_ANNOTATION 			= "pub_block_annotations";
	public static final String DB_TABLE_ANNOTATION_PK		= "pub_blk_ids_pk";
	public static final String DB_TABLE_ANNOTATION_FK		= "pub_blk_ids_fk";
	public static final String DB_TABLE_ANNOTATION_UK		= "pub_blk_ids_uk";
	public static final String DB_TABLE_ANNOTATION_IDX	= "pub_blk_ids_id_idx";

	public static final String DB_BLOCK_ID					= "blk_id";

	public static final String ATT_ARTICLE_ID				= "articleId";
	public static final String ATT_ARTICLE					= "article";
	public static final String ATT_ANNOTATONS_IDS			= "annotationIds";
	public static final String ATT_ANNOTATONS				= "annotations";

	
	@JsonProperty(
			value = RES_BLOCKTYPE,
			required = true)
	@Column(
			name = DB_BLOCKTYPE, 
			unique = false,
			nullable = false, 
			length = 16)
	@NotNull(
			message = "El tipo de bloque no puede ser nula")
	@Size(
			max = 16, 
			message = "El tipo de bloque no puede sobrepasar los {max} caracteres.")
	private String blockType;

	
	@JsonProperty(
			value = RES_OFFSET,
			required = false)
	@Column(
			name = DB_OFFSET, 
			unique = false,
			nullable = true)
	public Integer offset;

	
	@JsonProperty(
			value = RES_TEXT,
			required = true)
	@Column(
			name = DB_TEXT, 
			unique = false,
			nullable = true, 
			length = 8000)
	@Size(
			max = 8000, 
			message = "El texto no puede sobrepasar los {max} caracteres.")
	public String text;


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
			value = RES_ANNOTATONS,
			required = false)
	@CollectionTable(
			name = DB_TABLE_ANNOTATION, 
			joinColumns = @JoinColumn(
					name = DB_BLOCK_ID,
					referencedColumnName = DB_ID,
					foreignKey = @ForeignKey(
							value = ConstraintMode.NO_CONSTRAINT,
							name = DB_TABLE_ANNOTATION_FK)),
			uniqueConstraints = {
					@UniqueConstraint(
							name = DB_TABLE_ANNOTATION_PK, 
							columnNames = { DB_BLOCK_ID, PubAnnotationSubentity.DB_IDENTIFIER }) },
			indexes = {
					@Index(
							unique = true,
							name = DB_TABLE_ANNOTATION_IDX,
							columnList = DB_BLOCK_ID + ", " + PubAnnotationSubentity.DB_IDENTIFIER + ", " + PubAnnotationSubentity.DB_TYPE) })
	@ElementCollection(
			fetch = FetchType.EAGER)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Set<PubAnnotationSubentity> annotations;
	
	
	// CONSTRUCTOR -------------------------------------------------------------------------------------------

	public boolean mergeAnnotations(Set<PubAnnotationSubentity> items) {
		AtomicBoolean result = new AtomicBoolean(false);
		if ((items != null) && !items.isEmpty()) {
			if ((this.annotations != null) && (!this.annotations.isEmpty())) {
				items.forEach( item -> {
					if (!this.annotations.contains(item)) {
						result.set(true);
						this.annotations.add(item);
					}
				});
			} else {
				this.annotations = items;
			}
		}
		return result.get();
	}

}
