package es.rcs.tfm.db.model;

import java.time.LocalDate;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.ElementCollection;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.crnk.core.resource.annotations.JsonApiRelation;
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
		callSuper = false)
@Table(
		name = PubArticlePublicationEntity.DB_TABLE,
		uniqueConstraints = {
			@UniqueConstraint(
					name = PubArticlePublicationEntity.DB_ID_PK, 
					columnNames = { PubArticlePublicationKey.DB_ARTICLE_ID, PubArticlePublicationKey.DB_PUBLICATION_ID })  })
@Entity
@Audited
@EntityListeners(
		value = AuditingEntityListener.class)
public class PubArticlePublicationEntity {

	@Transient
	private static final Logger LOG = LoggerFactory.getLogger(PubFileEntity.class);

	public static final String RES_ACTION			= "articlesInPublication";
	public static final String RES_TYPE				= "ArticlesInPublication";

	public static final String RES_EDITIONTYPE		= "type";
	public static final String RES_VOLUME			= "name";
	public static final String RES_MEDIA_TYPE		= "mediaType";
	public static final String RES_NUMBER			= "number";
	public static final String RES_DATETYPE			= "dateType";
	public static final String RES_DATE				= "date";
	public static final String RES_YEAR				= "year";
	public static final String RES_YEAR_SESION		= "sesion";
	public static final String RES_START_PAGE		= "startPage";
	public static final String RES_END_PAGE			= "endPage";
	public static final String RES_REFERENCE		= "reference";
	public static final String RES_IDENTIFIERS		= "identifiers";
	public static final String RES_PUBLICATION_ID	= "publicationId";
	public static final String RES_PUBLICATION		= "publication";

	public static final String DB_TABLE 			= "pub_article_publication";
	public static final String DB_ID_PK 			= "pub_art_pub_pk";
	public static final String DB_ARTICLE_FK		= "pub_art_pub_art_fk";
	public static final String DB_PUBLICATION_FK	= "pub_art_pub_pub_fk";

	public static final String DB_EDITIONTYPE		= "jou_typ";
	public static final String DB_VOLUME			= "jou_txt";
	public static final String DB_MEDIA_TYPE		= "jou_med";
	public static final String DB_NUMBER			= "jou_num";
	public static final String DB_DATETYPE			= "jou_dty";
	public static final String DB_DATE				= "jou_dat";
	public static final String DB_YEAR				= "jou_yea";
	public static final String DB_YEAR_SESION		= "jou_yea_ses";
	public static final String DB_START_PAGE		= "jou_ini";
	public static final String DB_END_PAGE			= "jou_end";
	public static final String DB_REFERENCE			= "jou_ref";

	public static final String DB_TABLE_IDS 		= "pub_article_publication_loc";
	public static final String DB_TABLE_IDS_PK		= "pub_art_pub_loc_pk";
	public static final String DB_TABLE_IDS_FK		= "pub_art_pub_loc_fk";
	public static final String DB_TABLE_IDS_UK		= "pub_art_pub_loc_uk";
	public static final String DB_TABLE_IDS_IDX		= "pub_art_pub_loc_id_idx";

	public static final String DB_IDS_PUBLICATION_ID= "pub_id";
	public static final String DB_IDS_ARTICLE_ID	= "art_id";

	public static final String ATT_PUBLICATION_ID	= "publicationId";
	public static final String ATT_PUBLICATION		= "publication";

	
	@EmbeddedId
	private PubArticlePublicationKey id;

	
	@JsonProperty(
			value = RES_EDITIONTYPE,
			required = false)
	@Column(
			name = DB_EDITIONTYPE, 
			unique = false,
			nullable = true, 
			length = 256)
	@Size(
			max = 256, 
			message = "El tipo de revista no puede sobrepasar los {max} caracteres.")
	public String type;

	
	@JsonProperty(
			value = RES_VOLUME,
			required = false)
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
			value = RES_NUMBER,
			required = false)
	@Column(
			name = DB_NUMBER, 
			unique = false,
			nullable = true, 
			length = 64)
	@Size(
			max = 64, 
			message = "El numero no puede sobrepasar los {max} caracteres.")
	private String number;

	
	@JsonProperty(
			value = RES_DATETYPE,
			required = false)
	@Column(
			name = DB_DATETYPE, 
			unique = false,
			nullable = true, 
			length = 32)
	@Size(
			max = 32, 
			message = "El tipo de fecha no puede sobrepasar los {max} caracteres.")
	private String dateType;

	
	@JsonProperty(
			value = RES_DATE,
			required = false)
	@Column(
			name = DB_DATE, 
			unique = false,
			nullable = true)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private LocalDate date;

	
	@JsonProperty(
			value = RES_YEAR,
			required = false)
	@Column(
			name = DB_YEAR, 
			unique = false,
			nullable = true)
	private Integer year;

	
	@JsonProperty(
			value = RES_YEAR_SESION,
			required = false)
	@Column(
			name = DB_YEAR_SESION, 
			unique = false,
			nullable = true, 
			length = 32)
	@Size(
			max = 32, 
			message = "El indicador temporal no puede sobrepasar los {max} caracteres.")
	private String yearSesion;

	
	@JsonProperty(
			value = RES_START_PAGE,
			required = false)
	@Column(
			name = DB_START_PAGE, 
			unique = false,
			nullable = true, 
			length = 32)
	@Size(
			max = 32, 
			message = "La pagina inicial no puede sobrepasar los {max} caracteres.")
	private String startPage;

	
	@JsonProperty(
			value = RES_END_PAGE,
			required = false)
	@Column(
			name = DB_END_PAGE, 
			unique = false,
			nullable = true, 
			length = 32)
	@Size(
			max = 32, 
			message = "La pagina final no puede sobrepasar los {max} caracteres.")
	private String endPage;

	
	@JsonProperty(
			value = RES_REFERENCE,
			required = false)
	@Column(
			name = DB_REFERENCE, 
			unique = false,
			nullable = true, 
			length = 32)
	@Size(
			max = 32, 
			message = "La referencia no puede sobrepasar los {max} caracteres.")
	private String reference;

	
	@JoinColumn(
			name = PubArticlePublicationKey.DB_PUBLICATION_ID,
			referencedColumnName = PubPublicationEntity.DB_ID,
			foreignKey = @ForeignKey(
					name = DB_PUBLICATION_FK))
	@ManyToOne(
			optional = false,
			fetch = FetchType.LAZY,
			cascade = { CascadeType.DETACH })
	@MapsId(
			value = PubArticlePublicationKey.DB_PUBLICATION_ID)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private PubPublicationEntity publication;

	
	@JoinColumn(
			name = PubArticlePublicationKey.DB_ARTICLE_ID,
			referencedColumnName = PubArticleEntity.DB_ID,
			foreignKey = @ForeignKey(
					name = DB_ARTICLE_FK))
	@ManyToOne(
			optional = false,
			fetch = FetchType.LAZY,
			cascade = { CascadeType.DETACH })
	@MapsId(
			value = PubArticlePublicationKey.DB_ARTICLE_ID)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
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
			joinColumns = {
					@JoinColumn(
							name = DB_IDS_ARTICLE_ID,
							referencedColumnName = PubArticlePublicationKey.DB_ARTICLE_ID),
					@JoinColumn(
							name = DB_IDS_PUBLICATION_ID,
							referencedColumnName = PubArticlePublicationKey.DB_PUBLICATION_ID),
					},
			foreignKey = @ForeignKey(
							value = ConstraintMode.NO_CONSTRAINT,
							name = DB_TABLE_IDS_FK),
			uniqueConstraints = {
					@UniqueConstraint(
							name = DB_TABLE_IDS_PK, 
							columnNames = { DB_IDS_ARTICLE_ID, DB_IDS_PUBLICATION_ID, PubValuesSubentity.DB_TYPE }),
					@UniqueConstraint(
							name = DB_TABLE_IDS_UK, 
							columnNames = { DB_IDS_ARTICLE_ID, DB_IDS_PUBLICATION_ID, PubValuesSubentity.DB_VALUE, PubValuesSubentity.DB_TYPE }) },
			indexes = {
					@Index(
							unique = false,
							name = DB_TABLE_IDS_IDX,
							columnList = PubValuesSubentity.DB_VALUE + ", " + DB_IDS_ARTICLE_ID + ", " + DB_IDS_PUBLICATION_ID  + ", " + PubValuesSubentity.DB_TYPE) })
	@ElementCollection(
			fetch = FetchType.EAGER)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Set<PubValuesSubentity> locations;


	// CONSTRUCTOR -------------------------------------------------------------------------------------------

	public void setDate(final LocalDate date) {
		if (date != null) {
			this.date = date;
			this.year = date.getYear();
		}
	}

	public void setPublication(PubPublicationEntity item) {
		if (item != null) {
			this.publication = item;
			if (item.getId() != null) {
				if (this.id == null) this.id = new PubArticlePublicationKey();
				this.id.setPublicationId(item.getId());
			}
		}
	}

	public void setArticle(final PubArticleEntity item) {
		if (item != null) {
			this.article = item;
			if (item.getId() != null) {
				if (this.id == null) this.id = new PubArticlePublicationKey();
				this.id.setArticleId(item.getId());
			}
		}
	}

	public boolean mergeLocations(Set<PubValuesSubentity> items) {
		AtomicBoolean result = new AtomicBoolean(false);
		if ((items != null) && !items.isEmpty()) {
			if ((this.locations != null) && (!this.locations.isEmpty())) {
				items.forEach( item -> {
					if (!this.locations.contains(item)) {
						result.set(true);
						this.locations.add(item);
					}
				});
			} else {
				this.locations = items;
			}
		}
		return result.get();
	}
	
}
