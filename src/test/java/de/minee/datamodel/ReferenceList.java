package de.minee.datamodel;

import java.util.List;
import java.util.UUID;

public class ReferenceList {

	private UUID id;
	private List<RecursiveObject> recursiveObjects;
	private ReferenceChain user;
	private String name;
	private String description;

	public UUID getId() {
		return id;
	}

	public void setId(final UUID id) {
		this.id = id;
	}

	public List<RecursiveObject> getRecursiveObjects() {
		return recursiveObjects;
	}

	public void setRecursiveObjects(final List<RecursiveObject> recursiveObjects) {
		this.recursiveObjects = recursiveObjects;
	}

	public ReferenceChain getUser() {
		return user;
	}

	public void setUser(final ReferenceChain user) {
		this.user = user;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

}
