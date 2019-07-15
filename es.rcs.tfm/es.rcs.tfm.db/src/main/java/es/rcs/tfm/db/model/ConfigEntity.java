package es.rcs.tfm.db.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;

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
@Entity
@Audited
@Table(name="cfg_parameters")
@EntityListeners(AuditingEntityListener.class)
public class ConfigEntity extends AuditedBaseEntity {

	@Column(
			name = "parameter", 
			unique = false,
			nullable = false, 
			length = 32)
	public String parameter;

	@Column(
			name = "value", 
			unique = false,
			nullable = false, 
			length = 256)
	public String value;

	
}
