package de.minee.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HateoesContext {

	private final List<AbstractResource> resources = new ArrayList<>();
	private final Map<String, Class<?>> knownTypes = new HashMap<>();

	public Class<?> getTypeByName(final String typeName) {
		return knownTypes.get(typeName);
	}

	void addResource(final AbstractResource resource) {
		resources.add(resource);
	}

	List<AbstractResource> getResources() {
		return resources;
	}

	void addType(final Class<?> type) {
		knownTypes.put(type.getSimpleName(), type);
	}

}
