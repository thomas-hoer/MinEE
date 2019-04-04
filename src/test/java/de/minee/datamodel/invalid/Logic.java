package de.minee.datamodel.invalid;

import java.util.UUID;

public class Logic {

	private UUID id;
	private int mappedEnum;

	private enum Numbers {
		ONE, TWO, THREE;
	}

	public Numbers getMappedEnum() {
		switch (mappedEnum) {
		case 1:
			return Numbers.ONE;
		case 2:
			return Numbers.TWO;
		case 3:
			return Numbers.THREE;
		}
		return null;
	}

	public void setMappedEnum(final Numbers mappedEnum) {
		switch (mappedEnum) {
		case ONE:
			this.mappedEnum = 1;
		case TWO:
			this.mappedEnum = 2;
		case THREE:
			this.mappedEnum = 3;
		}
	}

	public UUID getId() {
		return id;
	}

	public void setId(final UUID id) {
		this.id = id;
	}

}
