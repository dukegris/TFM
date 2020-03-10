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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
		type = PubPublicationEntity.RES_TYPE,
		resourcePath = PubPublicationEntity.RES_ACTION,
		postable = false, patchable = false, deletable = false, 
		readable = true, sortable = true, filterable = true,
		pagingSpec = NumberSizePagingSpec.class )
@Table(
		name = PubPublicationEntity.DB_TABLE,
		uniqueConstraints = {
			@UniqueConstraint(
					name = PubPublicationEntity.DB_ID_PK, 
					columnNames = { PubPublicationEntity.DB_ID }),
			@UniqueConstraint(
					name = PubPublicationEntity.DB_UID_UK, 
					columnNames = { PubPublicationEntity.DB_UID }),
			@UniqueConstraint(
					name = PubPublicationEntity.DB_NAME_UK, 
					columnNames = { PubPublicationEntity.DB_TITLE })  })
@Entity
@Audited
@EntityListeners(
		value = AuditingEntityListener.class)
public class PubPublicationEntity extends AuditedBaseEntity {

	public static String BOOK_TYPE							= "LIBRO";
	public static String JOURNAL_TYPE						= "REVISTA";
	
	@Transient
	private static final Logger LOG = LoggerFactory.getLogger(PubFileEntity.class);

	public static final String RES_ACTION					= "publications";
	public static final String RES_TYPE						= "Publication";

	public static final String RES_PUBLICATIONTYPE			= "type";
	public static final String RES_SUBTYPE					= "subtype";
	public static final String RES_TITLE					= "title";
	public static final String RES_MEDIA					= "media";
	public static final String RES_ABBREVIATION				= "abbreviation";
	public static final String RES_COUNTRY					= "country";
	public static final String RES_IDENTIFIERS				= "identifiers";
	public static final String RES_ARTICLE_IDS				= "articleIds";
	public static final String RES_ARTICLES					= "articles";
	public static final String RES_REPORT					= "report";
	public static final String RES_SERIE					= "serie";
	public static final String RES_SERIE_TITLE				= "serieTitle";
	public static final String RES_VOLUME					= "volume";
	public static final String RES_VOLUME_TITLE				= "volumeTitle";
	public static final String RES_EDITION_EDITOR			= "editionEditor";
	public static final String RES_EDITION_TITLE			= "editionTitle";
	public static final String RES_EDITION_CITY				= "editionCity";
	public static final String RES_ARTICLE_AUTHOR_IDS		= "articleAuthorIds";
	public static final String RES_ARTICLE_AUTHORS			= "articleAuthors";

	public static final String DB_TABLE 					= "pub_publications";
	public static final String DB_ID_PK 					= "pub_pub_pk";
	public static final String DB_UID_UK					= "pub_pub_uid_uk";
	public static final String DB_NAME_UK					= "pub_pub_txt_uk";

	public static final String DB_PUBLICATIONTYPE			= "pub_typ";
	public static final String DB_SUBTYPE					= "pub_sub";
	public static final String DB_TITLE						= "pub_txt";
	public static final String DB_MEDIA						= "pub_med";
	public static final String DB_ABBREVIATION				= "jou_abr";
	public static final String DB_COUNTRY					= "jou_cou";
	public static final String DB_REPORT					= "bok_rep_txt";
	public static final String DB_SERIE						= "bok_ser_txt";
	public static final String DB_SERIE_TITLE				= "bok_ser_tit";
	public static final String DB_VOLUME					= "bok_vol_txt";
	public static final String DB_VOLUME_TITLE				= "bok_vol_tit";
	public static final String DB_EDITION_EDITOR			= "bok_edi_txt";
	public static final String DB_EDITION_TITLE				= "bok_edi_tit";
	public static final String DB_EDITION_CITY				= "bok_edi_ciu";

	public static final String DB_TABLE_IDS 				= "pub_publication_identifiers";
	public static final String DB_TABLE_IDS_FK				= "pub_pub_ids_fk";
	public static final String DB_TABLE_IDS_PK 				= "pub_pub_ids_pk";
	public static final String DB_TABLE_IDS_UK 				= "pub_pub_ids_val_uk";
	public static final String DB_TABLE_IDS_IDX				= "pub_pub_ids_id_idx";
	
	public static final String DB_PUBLICATION_ID			= "pub_id";
	
	public static final String ATT_ARTICLE_IDS				= "articleIds";
	public static final String ATT_ARTICLES					= "articles";
	public static final String ATT_ARTICLE_AUTHOR_IDS		= "articleAuthorIds";
	public static final String ATT_ARTICLE_AUTHORS			= "articleAuthors";

	
	@JsonProperty(
			value = RES_PUBLICATIONTYPE,
			required = false)
	@Column(
			name = DB_PUBLICATIONTYPE, 
			unique = false,
			nullable = true, 
			length = 32)
	@Size(
			max = 32, 
			message = "El tipo de publicacion no puede sobrepasar los {max} caracteres.")
	public String type;

	
	@JsonProperty(
			value = RES_SUBTYPE,
			required = false)
	@Column(
			name = DB_SUBTYPE, 
			unique = false,
			nullable = true, 
			length = 256)
	@Size(
			max = 256, 
			message = "El subtipo de publicacion no puede sobrepasar los {max} caracteres.")
	public String subtype;

	
	@JsonProperty(
			value = RES_TITLE,
			required = true)
	@Column(
			name = DB_TITLE, 
			unique = true,
			nullable = false, 
			length = 256)
	@NotNull(
			message = "El titulo de la publicacion no puede ser nulo")
	@Size(
			max = 256, 
			message = "El titulo de la publicacion no puede sobrepasar los {max} caracteres.")
	private String title;

	
	@JsonProperty(
			value = RES_MEDIA,
			required = false)
	@Column(
			name = DB_MEDIA, 
			unique = false,
			nullable = true, 
			length = 32)
	@Size(
			max = 32, 
			message = "El tipo de soporte no puede sobrepasar los {max} caracteres.")
	private String media;

	
	@JsonProperty(
			value = RES_ABBREVIATION,
			required = false)
	@Column(
			name = DB_ABBREVIATION, 
			unique = false,
			nullable = true, 
			length = 128)
	@Size(
			max = 128, 
			message = "La abreviatura no puede sobrepasar los {max} caracteres.")
	private String abbreviation;

	
	@JsonProperty(
			value = RES_COUNTRY,
			required = false)
	@Column(
			name = DB_COUNTRY, 
			unique = false,
			nullable = true, 
			length = 256)
	@Size(
			max = 256, 
			message = "El pais no puede sobrepasar los {max} caracteres.")
	private String country;
	@JsonProperty(
			value = RES_REPORT)
	@Column(
			name = DB_REPORT, 
			unique = false,
			nullable = true, 
			length = 256)
	@Size(
			max = 256, 
			message = "El informe no puede sobrepasar los {max} caracteres.")
	private String report;

	
	@JsonProperty(
			value = RES_VOLUME)
	@Column(
			name = DB_VOLUME, 
			unique = false,
			nullable = true, 
			length = 256)
	@Size(
			max = 256, 
			message = "El volumen no puede sobrepasar los {max} caracteres.")
	private String volume;

	
	@JsonProperty(
			value = RES_SERIE_TITLE)
	@Column(
			name = DB_SERIE_TITLE, 
			unique = false,
			nullable = true, 
			length = 256)
	@Size(
			max = 256, 
			message = "El titulo de la serie no puede sobrepasar los {max} caracteres.")
	private String serieTitle;

	
	@JsonProperty(
			value = RES_SERIE)
	@Column(
			name = DB_SERIE, 
			unique = false,
			nullable = true, 
			length = 256)
	@Size(
			max = 256, 
			message = "La serie no puede sobrepasar los {max} caracteres.")
	private String serie;

	
	@JsonProperty(
			value = RES_VOLUME_TITLE)
	@Column(
			name = DB_VOLUME_TITLE, 
			unique = false,
			nullable = true, 
			length = 256)
	@Size(
			max = 256, 
			message = "El titulo del volumenno puede sobrepasar los {max} caracteres.")
	private String volumeTitle;

	
	@JsonProperty(
			value = RES_EDITION_TITLE)
	@Column(
			name = DB_EDITION_TITLE, 
			unique = false,
			nullable = true, 
			length = 256)
	@Size(
			max = 256, 
			message = "La edicion no puede sobrepasar los {max} caracteres.")
	private String editionTitle;

	
	@JsonProperty(
			value = RES_EDITION_EDITOR)
	@Column(
			name = DB_EDITION_EDITOR, 
			unique = false,
			nullable = true, 
			length = 256)
	@Size(
			max = 256, 
			message = "La edicion no puede sobrepasar los {max} caracteres.")
	private String editionEditor;

	
	@JsonProperty(
			value = RES_EDITION_CITY)
	@Column(
			name = DB_EDITION_CITY, 
			unique = false,
			nullable = true, 
			length = 256)
	@Size(
			max = 256, 
			message = "La ciudad de edición no puede sobrepasar los {max} caracteres.")
	private String editionCity;

	
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
					name = DB_PUBLICATION_ID,
					referencedColumnName = DB_ID,
					unique = false,
					nullable = false,
					insertable = false,
					updatable = false,
					foreignKey = @ForeignKey(
							value = ConstraintMode.NO_CONSTRAINT,
							name = DB_TABLE_IDS_FK)),
			uniqueConstraints = {
					@UniqueConstraint(
							name = DB_TABLE_IDS_PK, 
							columnNames = { DB_PUBLICATION_ID, PubValuesSubentity.DB_TYPE }),
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
			value = RES_ARTICLE_IDS)
	@Transient
	private Set<Long> articleIds = null;


	@JsonApiRelation(
			idField = ATT_ARTICLE_IDS, 
			mappedBy = PubPublicationEntity.ATT_ARTICLES,
			serialize = SerializeType.EAGER,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
	@JsonProperty(
			value = RES_ARTICLES,
			required = false)
	@JoinTable (
			name = PubArticlePublicationEntity.DB_TABLE,
			//indexes = 
			joinColumns = @JoinColumn (
					name = PubArticlePublicationKey.DB_ARTICLE_ID, 
					referencedColumnName = DB_ID),
			inverseJoinColumns = @JoinColumn (
					name = PubArticlePublicationKey.DB_PUBLICATION_ID, 
					referencedColumnName = PubPublicationEntity.DB_ID),
			foreignKey = @ForeignKey (
					name = PubArticlePublicationEntity.DB_ARTICLE_FK),
			inverseForeignKey = @ForeignKey (
					name = PubArticlePublicationEntity.DB_PUBLICATION_FK))
	@ManyToMany(
			fetch = FetchType.LAZY,
			cascade = { CascadeType.ALL })
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<PubArticleEntity> articles;


	@JsonApiRelationId
	@JsonProperty(
			value = RES_ARTICLE_AUTHOR_IDS)
	@Transient
	private Set<Long> articlesAuthorIds = null;


	@JsonApiRelation(
			idField = ATT_ARTICLE_AUTHOR_IDS, 
			mappedBy = PubArticleAuthorEntity.ATT_PUBLICATION,
			serialize = SerializeType.EAGER,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
	@JsonProperty(
			value = RES_ARTICLE_AUTHORS,
			required = false)
	@OneToMany(
			mappedBy = PubArticleAuthorEntity.ATT_PUBLICATION,
			fetch = FetchType.LAZY,
			cascade = { CascadeType.ALL })
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<PubArticleAuthorEntity> articlesAuthors;

	
	// CONSTRUCTOR -------------------------------------------------------------------------------------------


	public void setArticlesAuthor(final Set<PubArticleAuthorEntity> items) {
		if ((items != null) && !items.isEmpty()) {
			this.articlesAuthors = items;
			Set<Long> itemIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
			if ((itemIds != null) && !itemIds.isEmpty()) {
				this.articlesAuthorIds = itemIds;
			}
		}
	}

	public void setArticles(final Set<PubArticleEntity> items) {
		if ((items != null) && !items.isEmpty()) {
			this.articles = items;
			Set<Long> itemIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
			if ((itemIds != null) && !itemIds.isEmpty()) {
				this.articleIds = itemIds;
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
