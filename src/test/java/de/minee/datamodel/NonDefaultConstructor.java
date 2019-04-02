package de.minee.datamodel;

import java.util.UUID;

public class NonDefaultConstructor {

	private final UUID id;

	public NonDefaultConstructor(final UUID id) {
		this.id = id;
	}

	public UUID getId() {
		return id;
	}
}
