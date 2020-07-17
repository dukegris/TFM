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
		type = PubAuthorEntity.RES_TYPE,
		resourcePath = PubFileEntity.RES_ACTION,
		postable = false, patchable = false, deletable = false, 
		readable = true, sortable = true, filterable = true,
		pagingSpec = NumberSizePagingSpec.class )
@Table(
		name = PubAuthorEntity.DB_TABLE,
		uniqueConstraints = {
			@UniqueConstraint(
					name = PubAuthorEntity.DB_ID_PK, 
					columnNames = { PubAuthorEntity.DB_ID }),
			@UniqueConstraint(
					name = PubAuthorEntity.DB_UID_UK, 
					columnNames = { PubAuthorEntity.DB_UID }) })
@Entity
@Audited
@EntityListeners(
		value = AuditingEntityListener.class)
public class PubAuthorEntity extends AuditedBaseEntity {

	@Transient
	private static final Logger LOG = LoggerFactory.getLogger(PubAuthorEntity.class);

	public static final String RES_ACTION					= "authors";
	public static final String RES_TYPE						= "Autor";

	public static final String RES_INITIALS					= "initials";
	public static final String RES_SUFFIX					= "suffix";
	public static final String RES_NAME						= "name";
	public static final String RES_LASTNAME					= "lastName";
	public static final String RES_GROUP					= "group";
	public static final String RES_IDENTIFIERS_IDS			= "identifierIds";
	public static final String RES_IDENTIFIERS				= "identifiers";
	public static final String RES_AUTHOR_BOOK_CENTRE_IDS	= "authorBookCentreIds";
	public static final String RES_AUTHOR_BOOK_CENTRE		= "authorBookCentres";
	/*
	public static final String RES_ARTICLE_IDS				= "articleIds";
	public static final String RES_ARTICLES					= "articles";
	public static final String RES_CENTRE_IDS				= "centreIds";
	public static final String RES_CENTRES					= "centres";
	*/

	public static final String DB_SEARCH_IDENTIFIERS 		= "author.searchByIdentifiers";

	public static final String DB_TABLE 					= "pub_authors";
	public static final String DB_ID_PK 					= "pub_aut_pk";
	public static final String DB_UID_UK					= "pub_aut_uid_uk";

	public static final String DB_INITIALS					= "aut_ini";
	public static final String DB_SUFFIX					= "aut_suf";
	public static final String DB_NAME						= "aut_nam";
	public static final String DB_LASTNAME					= "aut_lst";
	public static final String DB_GROUP						= "aut_grp";
	public static final String DB_IDENTIFIERS				= "aut_ids";

	public static final String DB_TABLE_IDS 				= "pub_author_identifiers";
	public static final String DB_TABLE_IDS_PK				= "pub_aut_ids_pk";
	public static final String DB_TABLE_IDS_FK				= "pub_aut_ids_fk";
	public static final String DB_TABLE_IDS_UK				= "pub_aut_ids_uk";
	public static final String DB_TABLE_IDS_IDX				= "pub_aut_ids_id_idx";
	
	public static final String DB_AUTHOR_ID					= "aut_id";

	public static final String ATT_IDENTIFIERS_IDS			= "identifierIds";
	public static final String ATT_IDENTIFIERS				= "identifiers";
	public static final String ATT_AUTHOR_BOOK_CENTRE_IDS	= "authorBookCentreIds";
	public static final String ATT_AUTHOR_BOOK_CENTRE		= "authorBookCentres";

	/*
	public static final String ATT_ARTICLE_IDS				= "articleIds";
	public static final String ATT_ARTICLES					= "articles";
	public static final String ATT_CENTRE_IDS				= "centreIds";
	public static final String ATT_CENTRES					= "centres";
	*/

	
	@JsonProperty(
			value = RES_INITIALS,
			required = false)
	@Column(
			name = DB_INITIALS, 
			unique = false,
			nullable = true, 
			length = 16)
	@Size(
			max = 16, 
			message = "Las iniciales no puede sobrepasar los {max} caracteres.")
	public String initials;

	
	@JsonProperty(
			value = RES_SUFFIX,
			required = false)
	@Column(
			name = DB_SUFFIX, 
			unique = false,
			nullable = true, 
			length = 32)
	@Size(
			max = 32, 
			message = "El sufijo no puede sobrepasar los {max} caracteres.")
	public String suffix;

	
	@JsonProperty(
			value = RES_NAME,
			required = true)
	@Column(
			name = DB_NAME, 
			unique = false,
			nullable = true, 
			length = 512)
	@Size(
			max = 512, 
			message = "El nombre no puede sobrepasar los {max} caracteres.")
	public String name;

	
	@JsonProperty(
			value = RES_LASTNAME,
			required = false)
	@Column(
			name = DB_LASTNAME, 
			unique = false,
			nullable = true, 
			length = 256)
	@Size(
			max = 256, 
			message = "Los apellidos no pueden sobrepasar los {max} caracteres.")
	public String lastName;

	
	@JsonProperty(
			value = RES_GROUP,
			required = false)
	@Column(
			name = DB_GROUP, 
			unique = false,
			nullable = true, 
			length = 256)
	@Size(
			max = 256, 
			message = "El grupo no puede sobrepasar los {max} caracteres.")
	public String group;


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
					name = DB_AUTHOR_ID,
					referencedColumnName = DB_ID,
					foreignKey = @ForeignKey(
							value = ConstraintMode.NO_CONSTRAINT,
							name = DB_TABLE_IDS_FK)),
			uniqueConstraints = {
					@UniqueConstraint(
							name = DB_TABLE_IDS_PK, 
							columnNames = { DB_AUTHOR_ID, PubValuesSubentity.DB_TYPE }),
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
	
	
	/*
	@JsonApiRelationId
	@JsonProperty(
			value = RES_ARTICLE_IDS)
	@Transient
	private Set<Long> articleIds = null;


	@JsonApiRelation(
			idField = ATT_ARTICLE_IDS, 
			mappedBy = PubArticleEntity.ATT_AUTHORS,
			serialize = SerializeType.EAGER,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
	@JsonProperty(
			value = RES_ARTICLES,
			required = false)
	@JoinTable (
			name = PubArticleAuthorEntity.DB_TABLE,
			//indexes = 
			joinColumns = @JoinColumn (
					name = PubArticleAuthorKey.DB_ARTICLE_ID, 
					referencedColumnName = PubArticleEntity.DB_ID,
					unique = false,
					insertable = false,
					updatable = false),
			inverseJoinColumns = @JoinColumn (
					name = PubArticleAuthorKey.DB_AUTHOR_ID, 
					referencedColumnName = PubAuthorEntity.DB_ID),
			foreignKey = @ForeignKey (
					name = PubArticleAuthorEntity.DB_ARTICLE_FK),
			inverseForeignKey = @ForeignKey (
					name = PubArticleAuthorEntity.DB_AUTHOR_FK))
	@ManyToMany(
			fetch = FetchType.LAZY,
			cascade = { CascadeType.DETACH })
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<PubArticleEntity> articles;


	@JsonApiRelationId
	@JsonProperty(
			value = RES_CENTRE_IDS)
	@Transient
	private Set<Long> centreIds = null;


	@JsonApiRelation(
			idField = ATT_CENTRE_IDS, 
			mappedBy = PubCenterEntity.ATT_AUTHORS,
			serialize = SerializeType.EAGER,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
	@JsonProperty(
			value = RES_CENTRES,
			required = false)
	@JoinTable (
			name = PubArticleAuthorEntity.DB_TABLE,
			joinColumns = @JoinColumn (
					name = PubArticleAuthorKey.DB_CENTRE_ID, 
					referencedColumnName = PubCenterEntity.DB_ID,
					unique = false,
					insertable = false,
					updatable = false),
			inverseJoinColumns = @JoinColumn (
					name = PubArticleAuthorKey.DB_AUTHOR_ID, 
					referencedColumnName = PubAuthorEntity.DB_ID),
			foreignKey = @ForeignKey (
					name = PubArticleAuthorEntity.DB_CENTRE_FK),
			inverseForeignKey = @ForeignKey (
					name = PubArticleAuthorEntity.DB_AUTHOR_FK))
	@ManyToMany(
			fetch = FetchType.LAZY,
			cascade = { CascadeType.DETACH })
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<PubCenterEntity> centres;
	*/


	@JsonApiRelationId
	@JsonProperty(
			value = RES_AUTHOR_BOOK_CENTRE_IDS)
	@Transient
	private Set<Long> authorBookCentreIds = null;


	@JsonApiRelation(
			idField = ATT_AUTHOR_BOOK_CENTRE_IDS, 
			mappedBy = PubArticleAuthorEntity.ATT_AUTHOR,
			serialize = SerializeType.EAGER,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
	@JsonProperty(
			value = RES_AUTHOR_BOOK_CENTRE,
			required = false)
	@OneToMany(
			mappedBy = PubArticleAuthorEntity.ATT_AUTHOR,
			fetch = FetchType.LAZY,
			cascade = { CascadeType.ALL })
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<PubArticleAuthorEntity> authorBookCentres;


	// CONSTRUCTOR -------------------------------------------------------------------------------------------

	/*
	public void setCentres(final Set<PubCenterEntity> items) {
		if ((items != null) && !items.isEmpty()) {
			this.centres = items;
			this.centreIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
		}
	}

	public void setArticles(final Set<PubArticleEntity> items) {
		if ((items != null) && !items.isEmpty()) {
			this.articles = items;
			this.articleIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
		}
	}
	*/

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
