package es.rcs.tfm.db.model;

import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
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
		type = PubTermEntity.RES_TYPE,
		resourcePath = PubTermEntity.RES_ACTION,
		postable = false, patchable = false, deletable = false, 
		readable = true, sortable = true, filterable = true,
		pagingSpec = NumberSizePagingSpec.class )
@Table(
		name = PubTermEntity.DB_TABLE,
		uniqueConstraints = {
			@UniqueConstraint(
					name = PubTermEntity.DB_ID_PK, 
					columnNames = { PubTermEntity.DB_ID }),
			@UniqueConstraint(
					name = PubTermEntity.DB_UID_UK, 
					columnNames = { PubTermEntity.DB_UID }),
			@UniqueConstraint(
					name = PubTermEntity.DB_NAME_UK, 
					columnNames = { PubTermEntity.DB_PROVIDER, PubTermEntity.DB_TERMTYPE, PubTermEntity.DB_CODE })  })
@Entity
@Audited
@EntityListeners(
		value = AuditingEntityListener.class)
public class PubTermEntity extends AuditedBaseEntity {

	@Transient
	private static final Logger LOG = LoggerFactory.getLogger(PubFileEntity.class);

	public static final String RES_ACTION			= "terms";
	public static final String RES_TYPE				= "Term";

	public static final String RES_PROVIDER			= "provider";
	public static final String RES_TERMTYPE			= "termType";
	public static final String RES_DESCTYPE			= "descType";
	public static final String RES_CODE				= "code";
	public static final String RES_VALUE			= "value";
	public static final String RES_PARENT_ID		= "parentId";
	public static final String RES_PARENT			= "parent";
	public static final String RES_TERM_IDS			= "termIds";
	public static final String RES_TERMS			= "terms";
	public static final String RES_ARTICLE_IDS		= "articleIds";
	public static final String RES_ARTICLES			= "articles";

	public static final String DB_TABLE 			= "pub_terms";
	public static final String DB_ID_PK 			= "pub_ter_pk";
	public static final String DB_UID_UK			= "pub_ter_uid_uk";
	public static final String DB_NAME_UK			= "pub_ter_txt_uk";
	public static final String DB_PARENT_ID_FK		= "pub_ter_id_fk";

	public static final String DB_PROVIDER			= "ter_prv";
	public static final String DB_TERMTYPE			= "ter_typ";
	public static final String DB_DESCTYPE			= "ter_des";
	public static final String DB_CODE				= "ter_cod";
	public static final String DB_VALUE				= "ter_val";
	public static final String DB_PARENT_ID			= "ter_id";
	
	public static final String ATT_PARENT_ID		= "parentId";
	public static final String ATT_PARENT			= "parent";
	public static final String ATT_TERM_IDS			= "termIds";
	public static final String ATT_TERMS			= "terms";
	public static final String ATT_ARTICLE_IDS		= "articleIds";
	public static final String ATT_ARTICLES			= "articles";

	
	@JsonProperty(
			value = RES_PROVIDER,
			required = true)
	@Column(
			name = DB_PROVIDER, 
			unique = false,
			nullable = false, 
			length = 16)
	@NotNull(
			message = "El proveedor del término no puede ser nulo")
	@Size(
			max = 16, 
			message = "El proveedor no puede sobrepasar los {max} caracteres.")
	public String provider;

	
	@JsonProperty(
			value = RES_TERMTYPE,
			required = true)
	@Column(
			name = DB_TERMTYPE, 
			unique = false,
			nullable = false, 
			length = 32)
	@NotNull(
			message = "El tipo descriptor/cualificador no puede ser nulo")
	@Size(
			max = 32, 
			message = "El tipo de termino no puede sobrepasar los {max} caracteres.")
	public String termtype;

	
	@JsonProperty(
			value = RES_DESCTYPE,
			required = false)
	@Column(
			name = DB_DESCTYPE, 
			unique = false,
			nullable = true, 
			length = 16)
	@Size(
			max = 16, 
			message = "El tipo suplementario no puede sobrepasar los {max} caracteres.")
	public String desctype;

	
	@JsonProperty(
			value = RES_CODE,
			required = true)
	@Column(
			name = DB_CODE, 
			unique = false,
			nullable = false, 
			length = 32)
	@NotNull(
			message = "El codigo no puede ser nulo")
	@Size(
			max = 32, 
			message = "El codigo no puede sobrepasar los {max} caracteres.")
	private String code;

	
	@JsonProperty(
			value = RES_VALUE,
			required = false)
	@Column(
			name = DB_VALUE, 
			unique = false,
			nullable = true, 
			length = 256)
	@Size(
			max = 256, 
			message = "El valor textual no puede sobrepasar los {max} caracteres.")
	private String value;

	
	@JsonApiRelationId
	@JsonProperty(
			value = RES_PARENT_ID)
	@Column(
			name = DB_PARENT_ID,
			unique = false,
			nullable = true)
	private Long parentId;

	
	@JsonApiRelation(
			idField = ATT_PARENT_ID,
			mappedBy = ATT_TERMS,
			lookUp = LookupIncludeBehavior.NONE,
			serialize = SerializeType.ONLY_ID)
	@JsonProperty(
			value = RES_PARENT)
	@JoinColumn(
			name = DB_PARENT_ID,
			referencedColumnName = DB_ID,
			unique = false,
			nullable = false,
			insertable = false,
			updatable = false,
			foreignKey = @ForeignKey(
					value = ConstraintMode.NO_CONSTRAINT,
					name = DB_PARENT_ID_FK))
	@ManyToOne(
			optional = false,
			fetch = FetchType.LAZY,
			cascade = { CascadeType.DETACH })
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private PubTermEntity parent;

	
	@JsonApiRelationId
	@JsonProperty(
			value = RES_TERM_IDS)
	@Transient
	private Set<Long> termIds = null;

	
	@JsonApiRelation(
			idField = ATT_TERM_IDS, 
			mappedBy = ATT_PARENT,
			serialize = SerializeType.EAGER,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
	@JsonProperty(
			value = RES_TERMS,
			required = false)
	@OneToMany(
			mappedBy =  ATT_PARENT,
			fetch = FetchType.LAZY,
			cascade = { CascadeType.ALL } )
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<PubTermEntity> terms;


	@JsonApiRelationId
	@JsonProperty(
			value = RES_ARTICLE_IDS)
	@Transient
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Set<Long> articleIds = null;


	@JsonApiRelation(
			idField = ATT_ARTICLE_IDS, 
			mappedBy = PubTermEntity.ATT_ARTICLES,
			serialize = SerializeType.EAGER,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
	@JsonProperty(
			value = RES_ARTICLES,
			required = false)
	@JoinTable (
			name = PubArticleTermEntity.DB_TABLE,
			//indexes = 
			joinColumns = @JoinColumn (
					name = PubArticleTermKey.DB_ARTICLE_ID, 
					referencedColumnName = DB_ID),
			inverseJoinColumns = @JoinColumn (
					name = PubArticleTermKey.DB_TERM_ID, 
					referencedColumnName = PubTermEntity.DB_ID),
			foreignKey = @ForeignKey (
					name = PubArticleTermEntity.DB_ARTICLE_FK),
			inverseForeignKey = @ForeignKey (
					name = PubArticleTermEntity.DB_TERM_FK))
	@ManyToMany(
			fetch = FetchType.LAZY,
			cascade = { CascadeType.ALL })
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<PubArticleEntity> articles;

	
	// CONSTRUCTOR -------------------------------------------------------------------------------------------
	
	public void setArticles(Set<PubArticleEntity> items) {
		if ((items != null) && !items.isEmpty()) {
			this.articles = items;
			Set<Long> itemIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
			if ((itemIds != null) && !itemIds.isEmpty()) {
				this.articleIds = itemIds;
			}
		}
	}
	
	public void setTerms(Set<PubTermEntity> items) {
		if ((items != null) && !items.isEmpty()) {
			this.terms = items;
			Set<Long> itemIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
			if ((itemIds != null) && !itemIds.isEmpty()) {
				this.termIds = itemIds;
			}
		}
	}
	
	public void setParent(PubTermEntity item) {
		if (item != null) {
			this.parent = item;
			if (item.getId() != null) {
				this.parentId = item.getId();
			}
		}
	}
	
}
