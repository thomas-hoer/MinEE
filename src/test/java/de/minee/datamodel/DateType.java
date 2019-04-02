package de.minee.datamodel;

import java.util.Date;
import java.util.UUID;

public class DateType {

	private UUID id;
	private Date date;

	public UUID getId() {
		return id;
	}

	public void setId(final UUID id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(final Date date) {
		this.date = date;
	}
}
