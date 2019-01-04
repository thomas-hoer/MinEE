package de.minee.datamodel;

import java.util.UUID;

public class SimpleReference {

	private UUID id;
	private ReferenceChain owner;
	private EnumObject content;
	private String name;
	private String value;// this is ment to be deleted on de.minee.datamodel.update.SimpleReference

	public UUID getId() {
		return id;
	}

	public void setId(final UUID id) {
		this.id = id;
	}

	public ReferenceChain getOwner() {
		return owner;
	}

	public void setOwner(final ReferenceChain owner) {
		this.owner = owner;
	}

	public EnumObject getContent() {
		return content;
	}

	public void setContent(final EnumObject content) {
		this.content = content;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
