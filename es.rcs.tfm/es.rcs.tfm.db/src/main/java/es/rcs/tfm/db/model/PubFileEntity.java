package es.rcs.tfm.db.model;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
		type = PubFileEntity.RES_TYPE,
		resourcePath = PubFileEntity.RES_ACTION,
		postable = false, patchable = false, deletable = false, 
		readable = true, sortable = true, filterable = true,
		pagingSpec = NumberSizePagingSpec.class )
@Table(
		name = PubFileEntity.DB_TABLE,
		uniqueConstraints = {
			@UniqueConstraint(
					name = PubFileEntity.DB_ID_PK, 
					columnNames = { PubFileEntity.DB_ID }),
			@UniqueConstraint(
					name = PubFileEntity.DB_UID_UK, 
					columnNames = { PubFileEntity.DB_UID }),
			@UniqueConstraint(
					name = PubFileEntity.DB_NAME_UK, 
					columnNames = { PubFileEntity.DB_SOURCE, PubFileEntity.DB_NAME })  })
@Entity
@Audited
@EntityListeners(
		value = AuditingEntityListener.class)
public class PubFileEntity extends AuditedBaseEntity {

	@Transient
	private static final Logger LOG = LoggerFactory.getLogger(PubFileEntity.class);

	public static final String FTP_PUBMED				= "FTP_PUBMED";
	public static final String FTP_PMC					= "FTP_PMC";
	public static final String FTP_PMC_TEXT				= "FTP_PMC_TEXT";
	
	public static final String PROCESSED				= "Fichero procesado";
	

	public static final String RES_ACTION				= "files";
	public static final String RES_TYPE					= "File";

	public static final String RES_SOURCE				= "source";
	public static final String RES_NAME					= "name";
	public static final String RES_GZ_DIR				= "gzdirectory";
	public static final String RES_GZ_FILE				= "gzname";
	public static final String RES_GZ_SIZE				= "gzsize";
	public static final String RES_GZ_TIMESTAMP			= "gztimestamp";
	public static final String RES_MD5_FILE				= "md5name";
	public static final String RES_NUM_ARTICLES			= "articlesTotal";
	public static final String RES_NUM_PROC_ARTICLES	= "articlesProcessed";
	public static final String RES_UNCOMPRESSED			= "uncompressedname";
	public static final String RES_ARTICLE_IDS			= "articleIds";
	public static final String RES_ARTICLES				= "articles";
	
	public static final String DB_TABLE 				= "pub_files";
	public static final String DB_ID_PK 				= "pub_fil_pk";
	public static final String DB_UID_UK				= "pub_fil_uid_uk";
	public static final String DB_NAME_UK				= "pub_fil_txt_uk";
	
	public static final String DB_SOURCE				= "fil_src";
	public static final String DB_NAME					= "fil_txt";
	public static final String DB_GZ_DIR				= "fil_gzd";
	public static final String DB_GZ_FILE				= "fil_gzf";
	public static final String DB_GZ_SIZE				= "fil_gzs";
	public static final String DB_GZ_TIMESTAMP			= "fil_gzt";
	public static final String DB_MD5_FILE				= "fil_md5";
	public static final String DB_UNCOMPRESSED			= "fil_unc";
	public static final String DB_NUM_ARTICLES			= "fil_nat";
	public static final String DB_NUM_PROC_ARTICLES		= "fil_nap";
	
	public static final String ATT_ARTICLE_IDS			= "articleIds";
	public static final String ATT_ARTICLES				= "articles";



	@JsonProperty(
			value = RES_SOURCE,
			required = true)
	@Column(
			name = DB_SOURCE, 
			unique = false,
			nullable = false, 
			length = 32)
	@NotNull(
			message = "El nombre no puede ser nulo")
	@Size(
			max = 32, 
			message = "El nombre no puede sobrepasar los {max} caracteres.")
	private String source;


	@JsonProperty(
			value = RES_NAME,
			required = true)
	@Column(
			name = DB_NAME, 
			unique = false,
			nullable = false, 
			length = 128)
	@NotNull(
			message = "El nombre no puede ser nulo")
	@Size(
			max = 128, 
			message = "El nombre no puede sobrepasar los {max} caracteres.")
	private String fileName;


	@JsonProperty(
			value = RES_GZ_DIR,
			required = true)
	@Column(
			name = DB_GZ_DIR, 
			unique = false,
			nullable = true, 
			length = 256)
	@Size(
			max = 256, 
			message = "El directorio no puede sobrepasar los {max} caracteres.")
	private String gzDirectory;


	@JsonProperty(
			value = RES_GZ_FILE,
			required = true)
	@Column(
			name = DB_GZ_FILE, 
			unique = false,
			nullable = true, 
			length = 128)
	@Size(
			max = 128, 
			message = "El fichero comprimido no puede sobrepasar los {max} caracteres.")
	private String gzFileName;


	@JsonProperty(
			value = RES_GZ_SIZE,
			required = true)
	@Column(
			name = DB_GZ_SIZE, 
			unique = false,
			nullable = true)
	private Long gzSize;


	@JsonProperty(
			value = RES_GZ_TIMESTAMP,
			required = true)
	@Column(
			name = DB_GZ_TIMESTAMP, 
			unique = false,
			nullable = true)
	private LocalDateTime gzTimeStamp;


	@JsonProperty(
			value = RES_MD5_FILE,
			required = true)
	@Column(
			name = DB_MD5_FILE, 
			unique = false,
			nullable = true, 
			length = 128)
	@Size(
			max = 128, 
			message = "El fichero MD5 no puede sobrepasar los {max} caracteres.")
	private String md5FileName;


	@JsonProperty(
			value = RES_UNCOMPRESSED,
			required = true)
	@Column(
			name = DB_UNCOMPRESSED, 
			unique = false,
			nullable = true, 
			length = 128)
	@Size(
			max = 128, 
			message = "El fichero descomprimido no puede sobrepasar los {max} caracteres.")
	private String uncompressedFileName;


	@JsonProperty(
			value = RES_NUM_ARTICLES)
	@Column(
			name = DB_NUM_ARTICLES, 
			unique = false,
			nullable = true)
	private Integer numArticlesTotal;


	@JsonProperty(
			value = RES_NUM_PROC_ARTICLES)
	@Column(
			name = DB_NUM_PROC_ARTICLES, 
			unique = false,
			nullable = true)
	private Integer numArticlesProcessed;
	
	@JsonApiRelationId
	@JsonProperty(
			value = RES_ARTICLE_IDS)
	@Transient
	private Set<Long> articleIds = null;


	@JsonApiRelation(
			idField = ATT_ARTICLE_IDS, 
			mappedBy = PubArticleEntity.ATT_FILES,
			serialize = SerializeType.EAGER,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
	@JsonProperty(
			value = RES_ARTICLES,
			required = false)
	@JoinTable (
			name = PubArticleFileEntity.DB_TABLE,
			//indexes = 
			joinColumns = @JoinColumn (
					name = PubArticleFileKey.DB_ARTICLE_ID, 
					referencedColumnName = PubArticleEntity.DB_ID),
			inverseJoinColumns = @JoinColumn (
					name = PubArticleFileKey.DB_FILE_ID, 
					referencedColumnName = PubFileEntity.DB_ID),
			foreignKey = @ForeignKey (
					name = PubArticleFileEntity.DB_ARTICLES_FK),
			inverseForeignKey = @ForeignKey (
					name = PubArticleFileEntity.DB_FILES_FK))
	@ManyToMany(
			fetch = FetchType.LAZY,
			cascade = { CascadeType.ALL })
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<PubArticleEntity> articles;


	// CONSTRUCTOR -------------------------------------------------------------------------------------------
	public PubFileEntity() {
		super();
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

}
