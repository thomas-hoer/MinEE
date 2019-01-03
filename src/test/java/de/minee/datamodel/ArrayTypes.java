package de.minee.datamodel;

import java.util.UUID;

public class ArrayTypes {

	private UUID id;
	private byte[] byteArray;
	private Integer[] intArray;

	public UUID getId() {
		return id;
	}

	public void setId(final UUID id) {
		this.id = id;
	}

	public Integer[] getIntArray() {
		return intArray;
	}

	public void setIntArray(final Integer[] intArray) {
		this.intArray = intArray;
	}

	public byte[] getByteArray() {
		return byteArray;
	}

	public void setByteArray(final byte[] byteArray) {
		this.byteArray = byteArray;
	}
}
