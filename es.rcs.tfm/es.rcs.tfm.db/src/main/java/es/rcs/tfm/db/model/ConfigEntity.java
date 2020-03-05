package es.rcs.tfm.db.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper=true)
@Table(
		name = ConfigEntity.DB_TABLE,
		uniqueConstraints = {
			@UniqueConstraint(
					name = ConfigEntity.DB_ID_PK, 
					columnNames = { ConfigEntity.DB_ID }),
			@UniqueConstraint(
					name = ConfigEntity.DB_UID_UK, 
					columnNames = { ConfigEntity.DB_UID }) })

@Entity
@Audited
@EntityListeners(
		value = AuditingEntityListener.class)
public class ConfigEntity extends AuditedBaseEntity {

	public static final String DB_TABLE						= "cfg_parameters";
	public static final String DB_ID_PK 					= "cfg_par_pk";
	public static final String DB_UID_UK					= "cfg_par_uid_uk";

	public static final String DB_PARAMETER					= "cfg_par";
	public static final String DB_VALUE						= "cfg_val";

	@Column(
			name = DB_PARAMETER, 
			unique = false,
			nullable = false, 
			length = 32)
	public String parameter;

	@Column(
			name = DB_VALUE, 
			unique = false,
			nullable = false, 
			length = 256)
	public String value;

	
}
