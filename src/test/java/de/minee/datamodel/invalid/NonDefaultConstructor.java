package de.minee.datamodel.invalid;

import java.util.UUID;

public class NonDefaultConstructor {

	private UUID id;

	public NonDefaultConstructor(final UUID id) {
		this.id = id;
	}

	public UUID getId() {
		return id;
	}

	public void setId(final UUID id) {
		this.id = id;
	}
}
