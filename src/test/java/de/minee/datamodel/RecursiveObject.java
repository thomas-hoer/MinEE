package de.minee.datamodel;

import java.util.UUID;

public class RecursiveObject {

	private UUID id;
	private RecursiveObject child;
	private String name;

	public UUID getId() {
		return id;
	}

	public void setId(final UUID id) {
		this.id = id;
	}

	public RecursiveObject getChild() {
		return child;
	}

	public void setChild(final RecursiveObject child) {
		this.child = child;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

}
