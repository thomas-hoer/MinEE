package de.minee.datamodel.invalid;

import java.util.UUID;

public class FieldPropertyMismatch {

	private UUID id;
	private Integer number;
	public UUID getId() {
		return id;
	}
	public void setId(final UUID id) {
		this.id = id;
	}
	public Boolean getBool() {
		return number == 1;
	}
	public void setBool(final Boolean number) {
		this.number = number ? 1 : 0;
	}
}
