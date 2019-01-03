package de.minee.datamodel;

import java.util.UUID;

import de.minee.datamodel.enumeration.Encryption;

public class PictureContent {

	private UUID id;
	private String physicalStorage;
	private Encryption encryption;

	public UUID getId() {
		return id;
	}

	public void setId(final UUID id) {
		this.id = id;
	}

	public String getPhysicalStorage() {
		return physicalStorage;
	}

	public void setPhysicalStorage(final String physicalStorage) {
		this.physicalStorage = physicalStorage;
	}

	public Encryption getEncryption() {
		return encryption;
	}

	public void setEncryption(final Encryption encryption) {
		this.encryption = encryption;
	}

}
