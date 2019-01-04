package de.minee.datamodel;

import java.util.UUID;

public class ReferenceChain {

	private UUID id;
	private String name;
	private SimpleReference simpleReference;

	public UUID getId() {
		return id;
	}

	public void setId(final UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public SimpleReference getSimpleReference() {
		return simpleReference;
	}

	public void setSimpleReference(SimpleReference simpleReference) {
		this.simpleReference = simpleReference;
	}
}
