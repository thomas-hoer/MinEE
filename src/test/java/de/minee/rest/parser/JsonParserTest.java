package de.minee.rest.parser;

import de.minee.datamodel.SimpleReference;
import de.minee.rest.parser.JsonParser;
import de.minee.rest.parser.Parser;
import de.minee.rest.parser.ParserException;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

public class JsonParserTest {

	private final JsonParser parser = new JsonParser();

	@Test
	public void testParseObject() throws ParserException {
		final Object instance = parser.parse("{}", Object.class);
		Assert.assertNotNull(instance);
	}

	@Test
	public void testParseSimpleReference() throws ParserException {
		final SimpleReference instance = parser.parse("{}", SimpleReference.class);
		Assert.assertNotNull(instance);
	}

	@Test
	public void testParseSimpleReference2() throws ParserException {
		final SimpleReference instance = parser.parse("{name:\"123\"}", SimpleReference.class);
		Assert.assertNotNull(instance);
		Assert.assertEquals("123", instance.getName());
	}

	@Test
	public void testParseSimpleReference3() throws ParserException {
		final SimpleReference instance = parser
				.parse("{name:\"123\", id:\"cd8ea07c-2274-485f-882d-f5093071f764\",value:abc}", SimpleReference.class);
		Assert.assertNotNull(instance);
		Assert.assertEquals("123", instance.getName());
		Assert.assertEquals("abc", instance.getValue());
		Assert.assertEquals(UUID.fromString("cd8ea07c-2274-485f-882d-f5093071f764"), instance.getId());
	}

	@Test
	public void testParseNullValue() throws ParserException {
		final SimpleReference instance = parser.parse("{name:null}", SimpleReference.class);
		Assert.assertNotNull(instance);
		Assert.assertNull(instance.getName());
		Assert.assertNull(instance.getId());
		Assert.assertNull(instance.getValue());
	}

	@Test
	public void testParseStringArray() throws ParserException {
		final String[] instance = parser.parse("[a,b,c]", String[].class);
		Assert.assertNotNull(instance);
		Assert.assertEquals(3, instance.length);
		Assert.assertEquals("a", instance[0]);
		Assert.assertEquals("b", instance[1]);
		Assert.assertEquals("c", instance[2]);
	}

	@Test
	public void testParseEscapeString() throws ParserException {
		final String instance = parser.parse("\"\\\"\"", String.class);
		Assert.assertNotNull(instance);
		Assert.assertEquals("\"", instance);
	}

	@Test
	public void testParseStringEmptyArray() throws ParserException {
		final Object[] instance = parser.parse("[]", Object[].class);
		Assert.assertNotNull(instance);
		Assert.assertEquals(0, instance.length);
	}

	@Test(expected = ParserException.class)
	public void testParseInvalidClass() throws ParserException {
		parser.parse("", Parser.class);
	}

	@Test(expected = ParserException.class)
	public void testParseInvalidArray() throws ParserException {
		parser.parse("[", Object[].class);
	}

	@Test(expected = ParserException.class)
	public void testParseInvalidPayload1() throws ParserException {
		parser.parse("\"", Object.class);
	}

	@Test(expected = ParserException.class)
	public void testParseInvalidPayload2() throws ParserException {
		parser.parse("\"a", Object.class);
	}
}
