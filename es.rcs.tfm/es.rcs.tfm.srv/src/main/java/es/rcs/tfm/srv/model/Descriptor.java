package es.rcs.tfm.srv.model;

import es.rcs.tfm.srv.model.Articulo.OwnerType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(
		callSuper = false)
public class Descriptor {

	private OwnerType owner;
	private String descriptor;

	public Descriptor(
			final OwnerType owner,
			final String descriptor) {
		super();
		this.owner = owner;
		this.descriptor = descriptor;
	}

}
