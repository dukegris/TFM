package es.rcs.tfm.api.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper=false)
public class Usuario {
	
	private String username;
	private String password;
	
	public Usuario() {
		super();
	}

	public Usuario(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}

}
