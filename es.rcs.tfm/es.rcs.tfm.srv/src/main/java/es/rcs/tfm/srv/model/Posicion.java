package es.rcs.tfm.srv.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(
		callSuper = false)
public class Posicion {
	private int offset = 0;
	private int length = 0;
	private int end = 0;
	public Posicion(int offset, int length) {
		super();
		this.offset = offset;
		this.setLength(length);
	}
	public void setOffset(int offset) {
		this.offset = offset;
		this.end = this.offset + this.length;
	}
	public void setLength(int length) {
		this.length = length;
		this.end = this.offset + this.length;
	}
	public void setEnd(int end) {
		this.end = end;
		this.length = this.end -this.offset;
	}
}
