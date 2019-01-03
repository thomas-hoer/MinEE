package de.minee.datamodel;

import java.util.UUID;

public class User {

	private UUID id;
	private String name;
	private String authentication;
	private Picture picture;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAuthentication() {
		return authentication;
	}

	public void setAuthentication(String authentication) {
		this.authentication = authentication;
	}

	public Picture getPicture() {
		return picture;
	}

	public void setPicture(Picture picture) {
		this.picture = picture;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", authentication=" + authentication + ", picture=" + picture
				+ "]";
	}
}
