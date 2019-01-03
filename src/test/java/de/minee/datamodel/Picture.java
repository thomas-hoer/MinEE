package de.minee.datamodel;

import java.util.UUID;

public class Picture {

	private UUID id;
	private User owner;
	private PictureContent content;
	private String name;
	private String description;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public PictureContent getContent() {
		return content;
	}

	public void setContent(PictureContent content) {
		this.content = content;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "Picture [id=" + id + ", owner=" + owner + ", content=" + content + ", name=" + name + ", description="
				+ description + "]";
	}
}
