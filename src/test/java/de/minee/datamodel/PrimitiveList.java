package de.minee.datamodel;

import java.util.List;
import java.util.UUID;

public class PrimitiveList {

	private UUID id;
	private List<Integer> intList;
	private List<String> stringList;
	private List<Boolean> boolList;

	public UUID getId() {
		return id;
	}

	public void setId(final UUID id) {
		this.id = id;
	}

	public List<Integer> getIntList() {
		return intList;
	}

	public void setIntList(final List<Integer> intList) {
		this.intList = intList;
	}

	public List<String> getStringList() {
		return stringList;
	}

	public void setStringList(final List<String> stringList) {
		this.stringList = stringList;
	}

	public List<Boolean> getBoolList() {
		return boolList;
	}

	public void setBoolList(final List<Boolean> boolList) {
		this.boolList = boolList;
	}

}
