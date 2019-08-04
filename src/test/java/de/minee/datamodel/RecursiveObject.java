package de.minee.datamodel;

import java.util.UUID;

public class RecursiveObject {

	private UUID id;
	private RecursiveObject child;
	private RecursiveObject child2;
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

	public RecursiveObject getChild2() {
		return child2;
	}

	public void setChild2(final RecursiveObject child2) {
		this.child2 = child2;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

}
