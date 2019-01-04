package de.minee.datamodel.update;

import java.util.UUID;

import de.minee.datamodel.EnumObject;

public class SimpleReference {

	private UUID id;
	private ReferenceChain owner;
	private EnumObject content;
	private String name;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public ReferenceChain getOwner() {
		return owner;
	}

	public void setOwner(ReferenceChain owner) {
		this.owner = owner;
	}

	public EnumObject getContent() {
		return content;
	}

	public void setContent(EnumObject content) {
		this.content = content;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Picture [id=" + id + ", owner=" + owner + ", content=" + content + ", name=" + name + "]";
	}
}
