package de.minee.datamodel.update;

import java.util.UUID;

import de.minee.datamodel.PictureContent;

public class Picture {

	private UUID id;
	private User owner;
	private PictureContent content;
	private String name;

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

	@Override
	public String toString() {
		return "Picture [id=" + id + ", owner=" + owner + ", content=" + content + ", name=" + name + "]";
	}
}
