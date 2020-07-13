package es.rcs.tfm.db.model;

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
		type = SecGroupEntity.RES_TYPE,
		resourcePath = SecGroupEntity.RES_ACTION,
		postable = false, patchable = false, deletable = false, 
		readable = true, sortable = true, filterable = true,
		pagingSpec = NumberSizePagingSpec.class )
@Table(
		name = SecGroupEntity.DB_TABLE,
		uniqueConstraints = {
			@UniqueConstraint(
				name = SecGroupEntity.DB_ID_PK, 
				columnNames = { SecGroupEntity.DB_ID }),
		@UniqueConstraint(
				name = SecGroupEntity.DB_UID_UK, 
				columnNames = { SecGroupEntity.DB_UID }),
		@UniqueConstraint(
				name = SecGroupEntity.DB_CODE_UK, 
				columnNames = { SecGroupEntity.DB_CODE }),
		@UniqueConstraint(
				name = SecGroupEntity.DB_GROUPNAME_UK, 
				columnNames = { SecGroupEntity.DB_GROUPNAME }) })
@Entity
@Audited
@EntityListeners(
		value = AuditingEntityListener.class)
public class SecGroupEntity extends AuditedBaseEntity {

	@Transient
	private static final Logger LOG = LoggerFactory.getLogger(SecGroupEntity.class);

	public static final String RES_ACTION			= "groups";
	public static final String RES_TYPE				= "Group";

	public static final String RES_CODE				= "code";
	public static final String RES_GROUPNAME		= "name";
	public static final String RES_USER_IDS			= "userIds";
	public static final String RES_USERS			= "users";
	public static final String RES_AUTHORITY_IDS	= "authorityIds";
	public static final String RES_AUTHORITIES		= "authorities";

	public static final String DB_TABLE 			= "sec_group";
	public static final String DB_ID_PK 			= "sec_grp_pk";
	public static final String DB_UID_UK			= "sec_grp_uid_uk";
	public static final String DB_CODE_UK			= "sec_grp_cod_uk";
	public static final String DB_GROUPNAME_UK		= "sec_grp_grp_uk";
	
	public static final String DB_CODE				= "grp_cod";
	public static final String DB_GROUPNAME			= "grp_txt";

	public static final String DB_TABLE_GROUP_AUTH	= "sec_group_authorities";
	public static final String DB_AUTHGROUPS_FK		= "sec_aut_grp_fk";
	public static final String DB_GROUPAUTHS_FK		= "sec_grp_aut_fk";

	public static final String DB_GROUP_ID			= "grp_id";
	public static final String DB_AUTH_ID			= "aut_id";
	
	public static final String ATT_CODE				= "code";
	public static final String ATT_GROUPNAME		= "name";
	public static final String ATT_AUTHORITIES		= "authorities";
	public static final String ATT_AUTHORITY_IDS	= "authorityIds";
	public static final String ATT_USERS			= "users";
	public static final String ATT_USER_IDS			= "userIds";

	
	@JsonProperty(
			value = RES_CODE,
			required = true)
	@Column(
			name = DB_CODE,
			unique = true,
			nullable = false,
			length = 32)
	@NotNull(
			message = "El codigo del grupo no puede ser nulo")
	@Size(
			max = 32, 
			message = "El codigo del grupo no puede sobrepasar los {max} caracteres.")
	private String code;

	
	@JsonProperty(
			value = RES_GROUPNAME,
			required = true)
	@Column(
			name = DB_GROUPNAME, 
			unique = true,
			nullable = false, 
			length = 256)
	@NotNull(
			message = "El nombre del grupo no puede ser nulo")
	@Size(
			max = 256, 
			message = "El nombre del grupo no puede sobrepasar los {max} caracteres.")
	private String name;

	
	@JsonApiRelationId
	@JsonProperty(
			value = RES_USER_IDS)
	@Transient
	private Set<Long> userIds = null;
	
	
	@JsonApiRelation(
			idField = ATT_USER_IDS, 
			mappedBy = SecUserEntity.ATT_GROUPS)
	@JsonProperty(
			value = RES_USERS)
	@ManyToMany(
			mappedBy = SecUserEntity.ATT_GROUPS,
			cascade = { CascadeType.DETACH },
			fetch = FetchType.LAZY)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<SecUserEntity> users = null;

	
	@JsonApiRelationId
	@JsonProperty(
			value = RES_AUTHORITY_IDS)
	@Transient
	private Set<Long> authorityIds = null;


	@JsonApiRelation(
			idField = ATT_AUTHORITY_IDS,
			mappedBy = SecAuthorityEntity.ATT_GROUPS)
	@JsonProperty(
			value = RES_AUTHORITIES)
	@JoinTable (
			name = DB_TABLE_GROUP_AUTH,
			joinColumns = @JoinColumn (
					name = DB_GROUP_ID, 
					referencedColumnName = DB_ID),
			inverseJoinColumns = @JoinColumn (
					name = DB_AUTH_ID, 
					referencedColumnName = SecAuthorityEntity.DB_ID),
			foreignKey = @ForeignKey (
					name = DB_GROUPAUTHS_FK),
			inverseForeignKey = @ForeignKey (
					name = DB_AUTHGROUPS_FK))
	@ManyToMany (
			// Associations marked as mappedBy must not define database mappings like @JoinTable or @JoinColumn		
			// mappedBy = "groups",
			fetch = FetchType.LAZY,
			cascade = { CascadeType.DETACH, CascadeType.PERSIST })
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<SecAuthorityEntity> authorities;


	// CONSTRUCTOR -------------------------------------------------------------------------------------------
	public SecGroupEntity() {
		super();
	}

	public SecGroupEntity(
			@NotNull(message = "El codigo del grupo no puede ser nulo") @Size(max = 32, message = "El codigo del grupo no puede sobrepasar los {max} carcateres.") String code,
			@NotNull(message = "El nombre del grupo no puede ser nulo") @Size(max = 256, message = "El nombre del grupo no puede sobrepasar los {max} carcateres.") String name,
			Set<SecAuthorityEntity> authorities) {
		super();
		this.code = code;
		this.name = name;
		this.setAuthorities(authorities);
	}

	public void setUsers(Set<SecUserEntity> items) {
		if ((items != null) && !items.isEmpty()) {
			this.users = items;
			Set<Long> itemIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
			if ((itemIds != null) && !itemIds.isEmpty()) {
				this.userIds = itemIds;
			}
		}
	}

	public void setAuthorities(Set<SecAuthorityEntity> items) {
		if ((items != null) && !items.isEmpty()) {
			this.authorities = items;
			Set<Long> itemIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
			if ((itemIds != null) && !itemIds.isEmpty()) {
				this.authorityIds = itemIds;
			}
		}
	}

}
