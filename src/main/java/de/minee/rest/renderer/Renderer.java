package de.minee.rest.renderer;

public interface Renderer {

	// TODO: Use InputStream and HateoesContext
	String render(Object input);

	String forCreate(Class<?> type);

	String forEdit(Object object);

	String getContentType();
}
