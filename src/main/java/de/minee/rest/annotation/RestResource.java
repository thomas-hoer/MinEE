package de.minee.rest.annotation;

import de.minee.rest.Operation;
import de.minee.rest.parser.Parser;
import de.minee.rest.renderer.Renderer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a Field as fully managed RestResource.
 *
 * <p>
 * If you create a RestServlet like the following example, a valid resource path
 * could be http://localhost:8080/rest/type/name depending on your context root.
 *
 * <pre>
 * &#64;WebServlet("/rest/*")
 * public class Rest extends RestServlet {
 * 	&#64;RestResource("/type/{name}")
 * 	MyType gallery;
 * }
 * </pre>
 * </p>
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface RestResource {

	/**
	 * Sub-Path of the Resource.
	 *
	 * @return Sub-Path of the Resource
	 */
	String value() default "";

	/**
	 * Sets the Http Operations that are allowed on the Resource.
	 *
	 * Allowed values or combinations are:
	 *
	 * GET Retrieve one Object or a list of Objects from the exact resource URI.
	 *
	 * PUT Create new or edit existing Object over the exact resource URI.
	 *
	 * POST Create new or edit existing Object over the sub path /create or /edit.
	 *
	 * DELETE Delete a specific Object or a group of Objects. The Objects that get
	 * deleted are the same that can be retrieved with GET under the same resource
	 * URI.
	 *
	 * ALL Allow all 4 Operations mentioned above.
	 *
	 * Default is ALL
	 *
	 * @return
	 */
	Operation[] allowedOperations() default { Operation.ALL };

	Class<? extends Parser>[] consumes() default {};

	Class<? extends Renderer>[] produces() default {};

}
