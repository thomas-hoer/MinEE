package de.minee.datamodel.invalid;

import java.util.UUID;

public class FinalFields {

	private final UUID id;
	private final String name;

	public FinalFields(final UUID id, final String name) {
		this.id = id;
		this.name = name;
	}
}
