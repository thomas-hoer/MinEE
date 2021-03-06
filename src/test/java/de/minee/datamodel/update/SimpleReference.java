package de.minee.datamodel.update;

import de.minee.datamodel.EnumObject;

import java.util.UUID;

public class SimpleReference {

	private UUID id;
	private ReferenceChain referenceChain;
	private EnumObject content;
	private String name;

	public UUID getId() {
		return id;
	}

	public void setId(final UUID id) {
		this.id = id;
	}

	public ReferenceChain setReferenceChain() {
		return referenceChain;
	}

	public void setReferenceChain(final ReferenceChain referenceChain) {
		this.referenceChain = referenceChain;
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

}
