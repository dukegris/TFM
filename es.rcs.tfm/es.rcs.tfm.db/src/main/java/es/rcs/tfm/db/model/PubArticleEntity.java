package es.rcs.tfm.db.model;

import java.time.LocalDate;
import java.util.HashSet;
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
import javax.persistence.Lob;
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
		type = PubArticleEntity.RES_TYPE,
		resourcePath = PubArticleEntity.RES_ACTION,
		postable = false, patchable = false, deletable = false, 
		readable = true, sortable = true, filterable = true,
		pagingSpec = NumberSizePagingSpec.class )
@Table(
		name = PubArticleEntity.DB_TABLE,
		uniqueConstraints = {
			@UniqueConstraint(
					name = PubArticleEntity.DB_ID_PK, 
					columnNames = { PubArticleEntity.DB_ID }),
			@UniqueConstraint(
					name = PubArticleEntity.DB_UID_UK, 
					columnNames = { PubArticleEntity.DB_UID })  })
@Entity
@Audited
@EntityListeners(
		value = AuditingEntityListener.class)
public class PubArticleEntity extends AuditedBaseEntity {

	@Transient
	private static final Logger LOG = LoggerFactory.getLogger(PubArticleEntity.class);	

	public static final String RES_ACTION					= "articles";
	public static final String RES_TYPE						= "Article";

	public static final String RES_PMID						= "pmid";
	public static final String RES_OWNER					= "owner";
	public static final String RES_TITLE					= "title";
	public static final String RES_VERNACULARTITLE			= "vernacularTitle";
	public static final String RES_SUMMARY					= "summary";
	public static final String RES_TEXT						= "text";
	public static final String RES_LANGUAGE					= "language";
	public static final String RES_MEDIA_TYPE				= "mediaType";
	public static final String RES_MD5_FILE					= "md5name";
	public static final String RES_UNCOMPRESSED				= "uncompressedname";
	public static final String RES_VERSION_ID				= "versionId";
	public static final String RES_VERSION_DATE				= "versionDate";
	public static final String RES_IDENTIFIERS				= "identifiers";
	public static final String RES_KEYWORDS					= "keywords";
	public static final String RES_PERMISSIONS				= "permissions";
	public static final String RES_DATES					= "dates";
	public static final String RES_PROPERTIES				= "properties";

	public static final String RES_REFERENCE_IDS			= "referenceIds";
	public static final String RES_REFERENCES				= "references";
	public static final String RES_FILE_IDS					= "fileIds";
	public static final String RES_FILES					= "files";
	public static final String RES_TERM_IDS					= "termIds";
	public static final String RES_TERMS					= "terms";
	public static final String RES_PUBLICATION_IDS			= "publicationIds";
	public static final String RES_PUBLICATIONS				= "publications";
	public static final String RES_PUBLICATION_AUTHOR_IDS	= "publicationAuthorIds";
	public static final String RES_PUBLICATION_AUTHORS		= "publicationAuthors";

	/*
	public static final String RES_AUTHOR_IDS				= "authorIds";
	public static final String RES_AUTHORS					= "authors";
	public static final String RES_CENTRE_IDS				= "centreIds";
	public static final String RES_CENTRES					= "centres";
	public static final String ATT_AUTHOR_IDS				= "authorIds";
	public static final String ATT_AUTHORS					= "authors";
	public static final String ATT_CENTRE_IDS				= "centreIds";
	public static final String ATT_CENTRES					= "centres";
	*/

	public static final String DB_TABLE 					= "pub_articles";
	public static final String DB_ID_PK 					= "pub_art_pk";
	public static final String DB_UID_UK					= "pub_art_uid_uk";
	public static final String DB_PMID_UK					= "pub_art_pmd_uk";
	public static final String DB_TITLE_UK					= "pub_art_tit_uk";
	
	public static final String DB_PMID						= "art_pmd";
	public static final String DB_OWNER						= "art_own";
	public static final String DB_TITLE						= "art_tit";
	public static final String DB_VERNACULARTITLE			= "art_ori";
	public static final String DB_SUMMARY					= "art_sum";
	public static final String DB_TEXT						= "art_txt";
	public static final String DB_LANGUAGE					= "art_lan";
	public static final String DB_MEDIA_TYPE				= "art_med";
	public static final String DB_VERSION_ID				= "art_ver_id";
	public static final String DB_VERSION_DATE				= "art_ver_dat";

	public static final String DB_TABLE_IDS 				= "pub_article_identifiers";
	public static final String DB_TABLE_IDS_PK				= "pub_art_ids_pk";
	public static final String DB_TABLE_IDS_FK				= "pub_art_ids_fk";
	public static final String DB_TABLE_IDS_UK				= "pub_art_ids_uk";
	public static final String DB_TABLE_IDS_IDX				= "pub_art_ids_idx";

	public static final String DB_TABLE_PERM 				= "pub_article_permissions";
	public static final String DB_TABLE_PERM_PK				= "pub_art_per_pk";
	public static final String DB_TABLE_PERM_FK				= "pub_art_per_fk";
	public static final String DB_TABLE_PERM_UK				= "pub_art_per_uk";
	public static final String DB_TABLE_PERM_IDX			= "pub_art_per_idx";

	public static final String DB_TABLE_PROPERTY			= "pub_article_properties";
	public static final String DB_TABLE_PROPERTY_PK			= "pub_art_pro_pk";
	public static final String DB_TABLE_PROPERTY_FK			= "pub_art_pro_fk";
	public static final String DB_TABLE_PROPERTY_UK			= "pub_art_pro_uk";
	public static final String DB_TABLE_PROPERTY_IDX		= "pub_art_pro_idx";
	
	public static final String DB_TABLE_KEY 				= "pub_article_keywords";
	public static final String DB_TABLE_KEY_PK				= "pub_art_key_pk";
	public static final String DB_TABLE_KEY_UK				= "pub_art_key_uk";
	public static final String DB_TABLE_KEY_FK				= "pub_art_key_fk";
	public static final String DB_TABLE_KEY_IDX				= "pub_art_key_idx";

	public static final String DB_TABLE_DATE				= "pub_article_dates";
	public static final String DB_TABLE_DATE_PK				= "pub_art_dat_pk";
	public static final String DB_TABLE_DATE_FK				= "pub_art_dat_fk";
	public static final String DB_TABLE_DATE_UK				= "pub_art_dat_uk";
	public static final String DB_TABLE_DATE_IDX			= "pub_art_dat_idx";

	public static final String DB_ARTICLE_ID				= "art_id";

	public static final String ATT_REFERENCE_IDS			= "referenceIds";
	public static final String ATT_REFERENCES				= "references";
	public static final String ATT_FILE_IDS					= "fileIds";
	public static final String ATT_FILES					= "files";
	public static final String ATT_TERM_IDS					= "termIds";
	public static final String ATT_TERMS					= "terms";
	public static final String ATT_PUBLICATION_IDS			= "publicationIds";
	public static final String ATT_PUBLICATIONS				= "publications";
	public static final String ATT_PUBLICATION_AUTHOR_IDS	= "publicationAuthorIds";
	public static final String ATT_PUBLICATION_AUTHORS		= "publicationAuthors";

	
	@JsonProperty(
			value = RES_OWNER,
			required = false)
	@Column(
			name = DB_OWNER, 
			unique = false,
			nullable = true, 
			length = 128)
	@Size(
			max = 128, 
			message = "El propietario no puede sobrepasar los {max} caracteres.")
	public String owner;

	
	@JsonProperty(
			value = RES_PMID,
			required = true)
	@Column(
			name = DB_PMID, 
			unique = false,
			nullable = false,
			length = 16)
	@NotNull(
			message = "El identificador no puede ser nulo")
	@Size(
			max = 16, 
			message = "El identificador no puede sobrepasar los {max} caracteres.")
	public String pmid;

	
	@JsonProperty(
			value = RES_TITLE,
			required = false)
	@Column(
			name = DB_TITLE, 
			unique = false,
			nullable = true, 
			length = 1024)
	@Size(
			max = 1024, 
			message = "El titulo no puede sobrepasar los {max} caracteres.")
	public String title;

	
	@JsonProperty(
			value = RES_VERNACULARTITLE,
			required = false)
	@Column(
			name = DB_VERNACULARTITLE, 
			unique = false,
			nullable = true, 
			length = 1024)
	@Size(
			max = 1024, 
			message = "El titulo en su idioma original no puede sobrepasar los {max} caracteres.")
	public String vernacularTitle;

	
	@JsonProperty(
			value = RES_SUMMARY,
			required = false)
	@Column(
			name = DB_SUMMARY, 
			unique = false,
			nullable = true, 
			length = 16384)
	@Size(
			max = 16384, 
			message = "El sumario no puede sobrepasar los {max} caracteres.")
	public String summary;

	
	@JsonProperty(
			value = RES_TEXT,
			required = false)
	@Lob
	@Column(
			name = DB_TEXT, 
			unique = false,
			nullable = true, 
			length = 65536)
	@Size(
			max = 65536, 
			message = "El texto no puede sobrepasar los {max} caracteres.")
	public String text;

	
	@JsonProperty(
			value = RES_LANGUAGE,
			required = false)
	@Column(
			name = DB_LANGUAGE, 
			unique = false,
			nullable = true, 
			length = 128)
	@Size(
			max = 128, 
			message = "El idioma no puede sobrepasar los {max} caracteres.")
	public String language;


	@JsonProperty(
			value = RES_MEDIA_TYPE,
			required = false)
	@Column(
			name = DB_MEDIA_TYPE, 
			unique = false,
			nullable = true, 
			length = 128)
	@Size(
			max = 128, 
			message = "El tipo de medio no puede sobrepasar los {max} caracteres.")
	public String mediaType;

	
	@JsonProperty(
			value = RES_VERSION_ID,
			required = false)
	@Column(
			name = DB_VERSION_ID, 
			unique = false,
			nullable = true)
	public Integer versionId;

	
	@JsonProperty(
			value = RES_VERSION_DATE,
			required = false)
	@Column(
			name = DB_VERSION_DATE, 
			unique = false,
			nullable = true)
	public LocalDate versionDate;


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
					name = DB_ARTICLE_ID,
					referencedColumnName = DB_ID,
					foreignKey = @ForeignKey(
							value = ConstraintMode.NO_CONSTRAINT,
							name = DB_TABLE_IDS_FK)),
			uniqueConstraints = {
					@UniqueConstraint(
							name = DB_TABLE_IDS_PK, 
							columnNames = { DB_ARTICLE_ID, PubValuesSubentity.DB_TYPE }),
					@UniqueConstraint(
							name = DB_TABLE_IDS_UK, 
							columnNames = { DB_ARTICLE_ID, PubValuesSubentity.DB_VALUE, PubValuesSubentity.DB_TYPE }) },
			indexes = {
					@Index(
							unique = false,
							name = DB_TABLE_IDS_IDX,
							columnList = PubValuesSubentity.DB_VALUE + ", " + DB_ARTICLE_ID + ", " + PubValuesSubentity.DB_TYPE) })
	@ElementCollection(
			fetch = FetchType.EAGER)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Set<PubValuesSubentity> identifiers;


	@JsonApiRelation(
			serialize = SerializeType.EAGER,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
	@JsonProperty(
			value = RES_DATES,
			required = false)
	@CollectionTable(
			name = DB_TABLE_DATE, 
			joinColumns = @JoinColumn(
					name = DB_ARTICLE_ID,
					referencedColumnName = DB_ID,
					foreignKey = @ForeignKey(
							value = ConstraintMode.NO_CONSTRAINT,
							name = DB_TABLE_DATE_FK)),
			uniqueConstraints = {
					@UniqueConstraint(
							name = DB_TABLE_DATE_PK, 
							columnNames = { DB_ARTICLE_ID, PubDateSubentity.DB_TYPE, PubDateSubentity.DB_DATE })  },
			indexes = {
					@Index(
							unique = false,
							name = DB_TABLE_DATE_IDX,
							columnList = PubDateSubentity.DB_DATE) })
	@ElementCollection(
			fetch = FetchType.EAGER)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Set<PubDateSubentity> dates;


	@JsonApiRelation(
			serialize = SerializeType.EAGER,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
	@JsonProperty(
			value = RES_PERMISSIONS,
			required = false)
	@CollectionTable(
			name = DB_TABLE_PERM, 
			joinColumns = @JoinColumn(
					name = DB_ARTICLE_ID,
					referencedColumnName = DB_ID,
					foreignKey = @ForeignKey(
							value = ConstraintMode.NO_CONSTRAINT,
							name = DB_TABLE_PERM_FK)),
			indexes = {
					@Index(
							unique = false,
							name = DB_TABLE_PERM_IDX,
							columnList = DB_ARTICLE_ID + "," + PubPermissionSubentity.DB_PERMISSION) })
	@ElementCollection(
			fetch = FetchType.EAGER)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Set<PubPermissionSubentity> permissions;


	@JsonApiRelation(
			serialize = SerializeType.EAGER,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
	@JsonProperty(
			value = RES_KEYWORDS,
			required = false)
	@CollectionTable(
			name = DB_TABLE_KEY, 
			joinColumns = @JoinColumn(
					name = DB_ARTICLE_ID,
					referencedColumnName = DB_ID,
					foreignKey = @ForeignKey(
							name = DB_TABLE_KEY_FK)),
			uniqueConstraints = {
					@UniqueConstraint(
							name = DB_TABLE_KEY_PK, 
							columnNames = { DB_ARTICLE_ID, PubKeywordSubentity.DB_VALUE, PubKeywordSubentity.DB_KEY }) },
			indexes = {
					@Index(
							unique = false,
							name = DB_TABLE_KEY_IDX,
							columnList = PubKeywordSubentity.DB_VALUE) })
	@ElementCollection(
			fetch = FetchType.EAGER)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Set<PubKeywordSubentity> keywords;


	@JsonApiRelation(
			serialize = SerializeType.EAGER,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
	@JsonProperty(
			value = RES_PROPERTIES,
			required = false)
	@CollectionTable(
			name = DB_TABLE_PROPERTY, 
			joinColumns = @JoinColumn(
					name = DB_ARTICLE_ID,
					referencedColumnName = DB_ID,
					foreignKey = @ForeignKey(
							value = ConstraintMode.NO_CONSTRAINT,
							name = DB_TABLE_PROPERTY_FK)),
			uniqueConstraints = {
					@UniqueConstraint(
							name = DB_TABLE_PROPERTY_PK, 
							columnNames = { DB_ARTICLE_ID, PubPropertySubentity.DB_OBJECT, PubPropertySubentity.DB_KEY }) },
			indexes = {
					@Index(
							unique = false,
							name = DB_TABLE_PROPERTY_IDX,
							columnList = PubPropertySubentity.DB_VALUE + ", " + PubPropertySubentity.DB_OBJECT) })
	@ElementCollection(
			fetch = FetchType.EAGER)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Set<PubPropertySubentity> properties;


	@JsonApiRelationId
	@JsonProperty(
			value = RES_REFERENCE_IDS)
	@Transient
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Set<Long> referenceIds = null;


	@JsonApiRelation(
			idField = ATT_REFERENCE_IDS, 
			mappedBy = PubReferenceSubentity.ATT_ARTICLE,
			serialize = SerializeType.LAZY,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
	@JsonProperty(
			value = RES_REFERENCES,
			required = false)
	@OneToMany(
			mappedBy = PubReferenceSubentity.ATT_ARTICLE,
			fetch = FetchType.LAZY,
			cascade = { CascadeType.ALL })
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<PubReferenceSubentity> references;


	@JsonApiRelationId
	@JsonProperty(
			value = RES_FILE_IDS)
	@Transient
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Set<Long> filesIds = null;


	@JsonApiRelation(
			idField = ATT_FILE_IDS, 
			mappedBy = PubFileEntity.ATT_ARTICLES,
			serialize = SerializeType.EAGER,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
	@JsonProperty(
			value = RES_FILES,
			required = false)
	@JoinTable (
			name = PubArticleFileEntity.DB_TABLE,
			//indexes = 
			joinColumns = @JoinColumn (
					name = PubArticleFileKey.DB_FILE_ID, 
					referencedColumnName = PubFileEntity.DB_ID),
			inverseJoinColumns = @JoinColumn (
					name = PubArticleFileKey.DB_ARTICLE_ID, 
					referencedColumnName = DB_ID),
			foreignKey = @ForeignKey (
					name = PubArticleFileEntity.DB_FILES_FK),
			inverseForeignKey = @ForeignKey (
					name = PubArticleFileEntity.DB_ARTICLES_FK))
	@ManyToMany(
			fetch = FetchType.LAZY,
			cascade = { CascadeType.ALL })
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<PubFileEntity> files;


	@JsonApiRelationId
	@JsonProperty(
			value = RES_TERM_IDS)
	@Transient
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Set<Long> termIds = null;


	@JsonApiRelation(
			idField = ATT_TERM_IDS, 
			mappedBy = PubTermEntity.ATT_ARTICLES,
			serialize = SerializeType.EAGER,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
	@JsonProperty(
			value = RES_TERMS,
			required = false)
	@JoinTable (
			name = PubArticleTermEntity.DB_TABLE,
			//indexes = 
			joinColumns = @JoinColumn (
					name = PubArticleTermKey.DB_TERM_ID, 
					referencedColumnName = PubTermEntity.DB_ID),
			inverseJoinColumns = @JoinColumn (
					name = PubArticleTermKey.DB_ARTICLE_ID, 
					referencedColumnName = DB_ID),
			foreignKey = @ForeignKey (
					name = PubArticleTermEntity.DB_TERM_FK),
			inverseForeignKey = @ForeignKey (
					name = PubArticleTermEntity.DB_ARTICLE_FK))
	@ManyToMany(
			fetch = FetchType.LAZY,
			cascade = { CascadeType.ALL })
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<PubTermEntity> terms;

	
	@JsonApiRelationId
	@JsonProperty(
			value = RES_PUBLICATION_IDS)
	@Transient
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Set<Long> publicationIds = null;


	@JsonApiRelation(
			idField = ATT_PUBLICATION_IDS, 
			mappedBy = PubPublicationEntity.ATT_ARTICLES,
			serialize = SerializeType.EAGER,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
	@JsonProperty(
			value = RES_PUBLICATIONS,
			required = false)
	@JoinTable (
			name = PubArticlePublicationEntity.DB_TABLE,
			//indexes = 
			joinColumns = @JoinColumn (
					name = PubArticlePublicationKey.DB_PUBLICATION_ID, 
					referencedColumnName = PubPublicationEntity.DB_ID),
			inverseJoinColumns = @JoinColumn (
					name = PubArticlePublicationKey.DB_ARTICLE_ID, 
					referencedColumnName = DB_ID),
			foreignKey = @ForeignKey (
					name = PubArticlePublicationEntity.DB_PUBLICATION_FK),
			inverseForeignKey = @ForeignKey (
					name = PubArticlePublicationEntity.DB_ARTICLE_FK))
	@ManyToMany(
			fetch = FetchType.LAZY,
			cascade = { CascadeType.ALL })
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<PubPublicationEntity> publications;


	@JsonApiRelationId
	@JsonProperty(
			value = RES_PUBLICATION_AUTHOR_IDS)
	@Transient
	private Set<Long> publicationAuthorIds = null;


	@JsonApiRelation(
			idField = ATT_PUBLICATION_AUTHOR_IDS, 
			mappedBy = PubArticleAuthorEntity.ATT_ARTICLE,
			serialize = SerializeType.EAGER,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
	@JsonProperty(
			value = RES_PUBLICATION_AUTHORS,
			required = false)
	@OneToMany(
			mappedBy = PubArticleAuthorEntity.ATT_ARTICLE,
			fetch = FetchType.LAZY,
			cascade = { CascadeType.ALL })
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<PubArticleAuthorEntity> publicationAuthors;


	// CONSTRUCTOR -------------------------------------------------------------------------------------------

	public void setPublications(final Set<PubPublicationEntity> items) {

		if (	(items == null) ||
				(items.isEmpty())) return;

		this.publications = items;
		Set<Long> itemIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
		if ((itemIds != null) && !itemIds.isEmpty()) {
			this.publicationIds = itemIds;
		}

	}
	
	public void setPublicationAuthors(final Set<PubArticleAuthorEntity> items) {

		if (	(items == null) ||
				(items.isEmpty())) return;

		this.publicationAuthors = items;
		Set<Long> itemIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
		if ((itemIds != null) && !itemIds.isEmpty()) {
			this.publicationAuthorIds = itemIds;
		}

	}

	public void setFiles(final Set<PubFileEntity> items) {

		if (	(items == null) ||
				(items.isEmpty())) return;

		this.files = items;
		Set<Long> itemIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
		if ((itemIds != null) && !itemIds.isEmpty()) {
			this.filesIds = itemIds;
		}

	}
	
	public boolean mergeFichero(PubFileEntity item) {

		if (	(item == null)) return false;

		boolean result = false;
		Set<PubFileEntity> list = this.getFiles();
		if ((list != null) && (!list.isEmpty())) {
			if (!list.contains(item)) {
				list.add(item);
				this.setFiles(list);
				result = true;
			};
		} else {
			list = new HashSet<PubFileEntity>();
			list.add(item);
			this.setFiles(list);
			result = true;
		}

		return result;

	}

	public void setReferences(final Set<PubReferenceSubentity> items) {

		if (	(items == null) ||
				(items.isEmpty())) return;
		
		this.references = items;
		Set<Long> itemIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
		if ((itemIds != null) && !itemIds.isEmpty()) {
			this.referenceIds = itemIds;
		}

	}
	
	public boolean mergeReferences(Set<PubReferenceSubentity> items) {

		if (	(items == null) ||
				(items.isEmpty())) return false;

		AtomicBoolean result = new AtomicBoolean(false);
		Set<PubReferenceSubentity> list = this.getReferences();
		if ((list != null) && (!list.isEmpty())) {
			items.forEach(item -> {
				if (!list.contains(item)) {
					for (PubReferenceSubentity instance: list) {
						if (instance.equals(item)) {
							result.set(instance.mergeIdentifiers(item.getIdentifiers()));
							break;
						}
					}
				} else {
					list.add(item);
					result.set(true);
				}
			});
			if (result.get()) setReferences(list);
		} else {
			result.set(true);
			setReferences(items);
		}

		return result.get();
		
	}
	
	public boolean mergeIdentifiers(Set<PubValuesSubentity> items) {

		if (	(items == null) ||
				(items.isEmpty())) return false;

		AtomicBoolean result = new AtomicBoolean(false);
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

		return result.get();
	}
	
	public boolean mergeDates(Set<PubDateSubentity> items) {

		if (	(items == null) ||
				(items.isEmpty())) return false;

		AtomicBoolean result = new AtomicBoolean(false);
		if ((this.dates != null) && (!this.dates.isEmpty())) {
			items.forEach( item -> {
				if (!this.dates.contains(item)) {
					result.set(true);
					this.dates.add(item);
				}
			});
		} else {
			this.dates = items;
		}

		return result.get();
		
	}
	
	public boolean mergePermissions(Set<PubPermissionSubentity> items) {

		if (	(items == null) ||
				(items.isEmpty())) return false;

		AtomicBoolean result = new AtomicBoolean(false);
		if ((this.permissions != null) && (!this.permissions.isEmpty())) {
			items.forEach( item -> {
				if (!this.permissions.contains(item)) {
					result.set(true);
					this.permissions.add(item);
				}
			});
		} else {
			this.permissions = items;
		}

		return result.get();
		
	}

	public boolean mergeKeywords(Set<PubKeywordSubentity> items) {

		if (	(items == null) ||
				(items.isEmpty())) return false;

		AtomicBoolean result = new AtomicBoolean(false);
		if ((this.keywords != null) && (!this.keywords.isEmpty())) {
			items.forEach( item -> {
				if (!this.keywords.contains(item)) {
					result.set(true);
					this.keywords.add(item);
				}
			});
		} else {
			this.keywords = items;
		}

		return result.get();

	}

	public boolean mergeProperties(Set<PubPropertySubentity> items) {

		if (	(items == null) ||
				(items.isEmpty())) return false;

		AtomicBoolean result = new AtomicBoolean(false);
		if ((this.properties != null) && (!this.properties.isEmpty())) {
			items.forEach( item -> {
				if (!this.properties.contains(item)) {
					result.set(true);
					this.properties.add(item);
				}
			});
		} else {
			this.properties = items;
		}

		return result.get();

	}

}
