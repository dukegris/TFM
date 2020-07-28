package es.rcs.tfm.srv.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(
		callSuper = false)
public class ArticuloBloquePosicion {

	private int offset = 0;
	private int length = 0;
	private int end = 0;

	public ArticuloBloquePosicion(
			final int offset,
			final int length) {
		super();
		this.offset = offset;
		this.setLength(length);
	}

	public void setOffset(
			final int offset) {
		this.offset = offset;
		this.end = this.offset + this.length;
	}

	public void setLength(
			final int length) {
		this.length = length;
		this.end = this.offset + this.length;
	}

	public void setEnd(
			final int end) {
		this.end = end;
		this.length = this.end -this.offset;
	}

}
