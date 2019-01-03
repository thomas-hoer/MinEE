package de.minee.datamodel.update;

import java.util.List;
import java.util.UUID;

import de.minee.datamodel.Category;

public class Gallery {

	private UUID id;
	private List<Category> categories;
	private User user;
	private String name;
	private String description;
	private List<Picture> pictures;

	public UUID getId() {
		return id;
	}

	public void setId(final UUID id) {
		this.id = id;
	}

	public List<Category> getCategories() {
		return categories;
	}

	public void setCategories(final List<Category> categories) {
		this.categories = categories;
	}

	public User getUser() {
		return user;
	}

	public void setUser(final User user) {
		this.user = user;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public List<Picture> getPictures() {
		return pictures;
	}

	public void setPictures(final List<Picture> pictures) {
		this.pictures = pictures;
	}

}
