package de.minee.datamodel;

import java.util.UUID;

import de.minee.datamodel.enumeration.Enumeration;

public class EnumObject {

	private UUID id;
	private String string;
	private Enumeration enumeration;

	public UUID getId() {
		return id;
	}

	public void setId(final UUID id) {
		this.id = id;
	}

	public String getString() {
		return string;
	}

	public void setString(final String string) {
		this.string = string;
	}

	public Enumeration getEnumeration() {
		return enumeration;
	}

	public void setEnumeration(final Enumeration enumeration) {
		this.enumeration = enumeration;
	}

}
