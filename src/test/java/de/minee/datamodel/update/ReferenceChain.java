package de.minee.datamodel.update;

import java.util.UUID;

public class ReferenceChain {

	private UUID id;
	private String name;
	private String authentication;
	private SimpleReference picture;
	private String email;

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

	public SimpleReference getPicture() {
		return picture;
	}

	public void setPicture(SimpleReference picture) {
		this.picture = picture;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", authentication=" + authentication + ", picture=" + picture
				+ ", email=" + email + "]";
	}

}
