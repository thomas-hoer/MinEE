package de.minee.datamodel;

import java.util.UUID;

public class SimpleReference {

	private UUID id;
	private ReferenceChain referenceChain;
	private EnumObject content;
	private String name;
	private String value;// this is ment to be deleted on de.minee.datamodel.update.SimpleReference

	public UUID getId() {
		return id;
	}

	public void setId(final UUID id) {
		this.id = id;
	}

	public ReferenceChain getReferenceChain() {
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

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}

}
