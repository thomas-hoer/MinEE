package de.minee.hateoes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import de.minee.datamodel.ReferenceList;
import de.minee.hateoes.parser.JsonParser;
import de.minee.hateoes.renderer.JsonRenderer;
import de.minee.hateoes.renderer.Renderer;
import de.minee.jpa.DAOTestImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.h2.util.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class HateoesServletTest {
	private static HateoesServlet HATEOES_TEST_SERVLET = new HateoesServlet() {
		private static final long serialVersionUID = -6669308238856259151L;

		@HateoesResource("rlist/{id}")
		@Persistent
		ReferenceList referenceList;

		@DataAccessObject
		DAOTestImpl daoImpl;
	};

	@BeforeClass
	public static void initServlet() throws ServletException {
		HATEOES_TEST_SERVLET.init();
	}

	@Test
	public void testRoot() throws IOException {
		final MockHttpServletRequestImpl request = new MockHttpServletRequestImpl();
		final MockHttpServletResponseImpl response = new MockHttpServletResponseImpl();
		HATEOES_TEST_SERVLET.service(request, response);

		final String output = response.getWrittenOutput();
		assertNotNull(output);
		assertTrue(output.contains("rlist/{id}"));
	}

	@Test(expected = HateoesException.class)
	public void testInitPersistentWithoutDAO() throws ServletException {
		final HateoesServlet hateoesServlet = new HateoesServlet() {

			private static final long serialVersionUID = -2342152424641422998L;
			@HateoesResource("rlist/{id}/")
			@Persistent
			ReferenceList referenceList;

		};
		hateoesServlet.init();
	}

	@Test
	public void testInitInMemResource() throws ServletException, IOException {
		final HateoesServlet hateoesServlet = createInMemServlet();

		MockHttpServletRequestImpl request;
		MockHttpServletResponseImpl response;

		// Check for empty Database
		request = new MockHttpServletRequestImpl("rlists");
		response = new MockHttpServletResponseImpl();
		hateoesServlet.service(request, response);
		final String outputCheckEmpty = response.getWrittenOutput();

		// Insert new Elements
		request = new MockHttpServletRequestImpl("rlists/create", Operation.POST, "");
		response = new MockHttpServletResponseImpl();
		hateoesServlet.service(request, response);
		request = new MockHttpServletRequestImpl("rlists/create", Operation.POST, "");
		response = new MockHttpServletResponseImpl();
		hateoesServlet.service(request, response);
		final String outputPostResource = response.getWrittenOutput();

		assertTrue(StringUtils.isNullOrEmpty(outputPostResource));
		assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
		assertFalse(StringUtils.isNullOrEmpty(response.getHeader("id")));

		final UUID id = UUID.fromString(response.getHeader("id"));

		// Check again all entries of ReferenceList in Database
		request = new MockHttpServletRequestImpl("rlists");
		response = new MockHttpServletResponseImpl();
		hateoesServlet.service(request, response);
		final String outputCheckResourceIsPersisted = response.getWrittenOutput();

		assertNotNull(outputCheckEmpty);
		assertFalse(outputCheckEmpty.contains("ReferenceList"));

		assertNotNull(outputCheckResourceIsPersisted);
		assertTrue(outputCheckResourceIsPersisted.contains("ReferenceList"));
		// Two elements found
		assertEquals(3, outputCheckResourceIsPersisted.split("ReferenceList").length);

		request = new MockHttpServletRequestImpl("rlist/" + id.toString());
		response = new MockHttpServletResponseImpl();
		hateoesServlet.service(request, response);
		final String selectOutput = response.getWrittenOutput();

		assertNotNull(selectOutput);
		// One element found
		assertEquals(2, selectOutput.split("ReferenceList").length);

		request = new MockHttpServletRequestImpl("rlist/000");
		response = new MockHttpServletResponseImpl();
		hateoesServlet.service(request, response);
		final String selectOutputNoElement = response.getWrittenOutput();
		assertNotNull(selectOutputNoElement);
		// No element found
		assertEquals(1, selectOutputNoElement.split("ReferenceList").length);
	}

	@Test
	public void testGetCreate() throws IOException, ServletException {
		final HateoesServlet hateoesServlet = createInMemServlet();
		final MockHttpServletRequestImpl request = new MockHttpServletRequestImpl("rlists/create");
		final MockHttpServletResponseImpl response = new MockHttpServletResponseImpl();
		hateoesServlet.service(request, response);
		final String output = response.getWrittenOutput();
		assertNotNull(output);
		assertTrue(output.contains("id"));
		assertTrue(output.contains("recursiveObjects"));
		assertTrue(output.contains("user"));
		assertTrue(output.contains("name"));
		assertTrue(output.contains("description"));

	}

	@Test
	public void testGetCreateNonExistent() throws IOException, ServletException {
		final HateoesServlet hateoesServlet = createInMemServlet(null);
		final MockHttpServletRequestImpl request = new MockHttpServletRequestImpl("rlists/create");
		final MockHttpServletResponseImpl response = new MockHttpServletResponseImpl();
		hateoesServlet.service(request, response);
		assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getError());
	}

	@Test
	public void testGetCreateNullResource() throws IOException, ServletException {
		final HateoesServlet hateoesServlet = createInMemServlet(null);
		final MockHttpServletRequestImpl request = new MockHttpServletRequestImpl(null);
		final MockHttpServletResponseImpl response = new MockHttpServletResponseImpl();
		hateoesServlet.service(request, response);
		assertEquals(HttpServletResponse.SC_OK, response.getError());
		assertEquals("[]", response.getWrittenOutput());
	}

	@Test
	public void testPostCreate() throws ServletException, IOException {
		final HateoesServlet hateoesServlet = createInMemServlet();
		MockHttpServletRequestImpl request;
		MockHttpServletResponseImpl response;

		request = new MockHttpServletRequestImpl("rlists/create", Operation.POST, null);
		response = new MockHttpServletResponseImpl();
		final Map<String, String> parameters = new HashMap<>();
		parameters.put("name", "Name!");
		parameters.put("description", "Desc1");
		request.addParameters(parameters);
		hateoesServlet.service(request, response);
		final UUID id = UUID.fromString(response.getHeader("id"));
		assertNotNull(id);

		request = new MockHttpServletRequestImpl("rlists");
		response = new MockHttpServletResponseImpl();
		hateoesServlet.service(request, response);
		final String output = response.getWrittenOutput();
		assertNotNull(output);
		assertTrue(output.contains("Name!"));
		assertTrue(output.contains("Desc1"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPostCreateTransformationException() throws ServletException, IOException {
		final HateoesServlet hateoesServlet = createInMemServlet();
		final MockHttpServletRequestImpl request = new MockHttpServletRequestImpl("rlists/create", Operation.POST,
				null);
		final MockHttpServletResponseImpl response = new MockHttpServletResponseImpl();

		final Map<String, String> parameters = new HashMap<>();
		parameters.put("id", "ZZZ");
		request.addParameters(parameters);
		hateoesServlet.service(request, response);
	}

	@Test
	public void testGetEdit() throws IOException, ServletException {
		final HateoesServlet hateoesServlet = createInMemServlet();
		MockHttpServletRequestImpl request;
		MockHttpServletResponseImpl response;
		request = new MockHttpServletRequestImpl("rlists/create", Operation.POST, "");
		response = new MockHttpServletResponseImpl();
		hateoesServlet.service(request, response);

		final UUID id = UUID.fromString(response.getHeader("id"));

		request = new MockHttpServletRequestImpl("rlist/" + id + "/edit");
		response = new MockHttpServletResponseImpl();
		hateoesServlet.service(request, response);
		final String output = response.getWrittenOutput();
		assertNotNull(output);
		assertTrue(output.contains(id.toString()));
	}

	@Test
	public void testPostEdit() throws IOException, ServletException {
		final HateoesServlet hateoesServlet = createInMemServlet();
		MockHttpServletRequestImpl request;
		MockHttpServletResponseImpl response;
		request = new MockHttpServletRequestImpl("rlists/create", Operation.POST, "");
		request.addParameter("name", "old name");
		response = new MockHttpServletResponseImpl();
		hateoesServlet.service(request, response);
		final UUID id = UUID.fromString(response.getHeader("id"));

		request = new MockHttpServletRequestImpl("rlist/" + id + "/edit", Operation.POST, "");
		request.addParameter("id", id.toString());
		request.addParameter("name", "new name");
		response = new MockHttpServletResponseImpl();
		hateoesServlet.service(request, response);
		final String editOutput = response.getWrittenOutput();
		assertTrue(editOutput.contains("1 Element(s) updated"));
		assertEquals(HttpServletResponse.SC_OK, response.getError());

		request = new MockHttpServletRequestImpl("rlist/" + id);
		response = new MockHttpServletResponseImpl();
		hateoesServlet.service(request, response);
		final String output = response.getWrittenOutput();
		assertTrue(output.contains("new name"));
	}

	@Test
	public void testGetEditConflict() throws IOException, ServletException {
		final HateoesServlet hateoesServlet = createInMemServlet();
		MockHttpServletRequestImpl request;
		MockHttpServletResponseImpl response;
		request = new MockHttpServletRequestImpl("rlists/create", Operation.POST, "");
		response = new MockHttpServletResponseImpl();
		hateoesServlet.service(request, response);
		request = new MockHttpServletRequestImpl("rlists/create", Operation.POST, "");
		response = new MockHttpServletResponseImpl();
		hateoesServlet.service(request, response);

		request = new MockHttpServletRequestImpl("rlists/edit");
		response = new MockHttpServletResponseImpl();
		hateoesServlet.service(request, response);
		response.getWrittenOutput();
		assertEquals(HttpServletResponse.SC_CONFLICT, response.getError());
	}

	@Test
	public void testGetEditNotFound() throws IOException, ServletException {
		final HateoesServlet hateoesServlet = createInMemServlet();
		MockHttpServletRequestImpl request;
		MockHttpServletResponseImpl response;

		request = new MockHttpServletRequestImpl("rlist/" + UUID.randomUUID() + "/edit");
		response = new MockHttpServletResponseImpl();
		hateoesServlet.service(request, response);
		response.getWrittenOutput();
		assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getError());
	}

	@Test
	public void testGetEditError() throws IOException, ServletException {
		final HateoesServlet hateoesServlet = createInMemServlet();
		MockHttpServletRequestImpl request;
		MockHttpServletResponseImpl response;
		request = new MockHttpServletRequestImpl("rlists/foo");
		response = new MockHttpServletResponseImpl();
		hateoesServlet.service(request, response);

		assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getError());
	}

	@Test
	public void testJsonServlet() throws IOException, ServletException {
		final Renderer renderer = new JsonRenderer();
		final HateoesServlet hateoesServlet = createJsonServlet();
		final UUID uuid = UUID.randomUUID();
		final MockHttpServletRequestImpl request1 = new MockHttpServletRequestImpl(uuid.toString());
		final MockHttpServletResponseImpl response1 = new MockHttpServletResponseImpl();
		hateoesServlet.service(request1, response1);
		assertEquals(HttpServletResponse.SC_NOT_FOUND, response1.getError());

		final MockHttpServletRequestImpl request2 = new MockHttpServletRequestImpl(uuid.toString() + "/create");
		final MockHttpServletResponseImpl response2 = new MockHttpServletResponseImpl();
		hateoesServlet.service(request2, response2);
		assertEquals(HttpServletResponse.SC_OK, response2.getError());
		assertEquals(renderer.forCreate(ReferenceList.class), response2.getWrittenOutput());

		final String newJsonObject = "{}";
		final MockHttpServletRequestImpl request3 = new MockHttpServletRequestImpl(uuid.toString() + "/create",
				Operation.POST, newJsonObject);
		final MockHttpServletResponseImpl response3 = new MockHttpServletResponseImpl();
		hateoesServlet.service(request3, response3);
		assertEquals(HttpServletResponse.SC_CREATED, response3.getError());
		final String newId = response3.getHeader("id");
		assertFalse(StringUtils.isNullOrEmpty(newId));
		assertEquals("", response3.getWrittenOutput());

		final MockHttpServletRequestImpl request4 = new MockHttpServletRequestImpl(newId);
		final MockHttpServletResponseImpl response4 = new MockHttpServletResponseImpl();
		hateoesServlet.service(request4, response4);
		assertEquals(HttpServletResponse.SC_OK, response4.getStatus());
		assertEquals(String.format("{id:\"%s\",recursiveObjects:[]}", newId), response4.getWrittenOutput());

	}

	private static HateoesServlet createInMemServlet() throws ServletException {
		final HateoesServlet servlet = new HateoesServlet() {

			private static final long serialVersionUID = -1207374138713223126L;

			@HateoesResource("rlists")
			ReferenceList referenceLists;

			@HateoesResource("rlist/{id}/")
			ReferenceList referenceList;
		};
		servlet.init();
		return servlet;
	}

	private static HateoesServlet createJsonServlet() throws ServletException {
		final HateoesServlet servlet = new HateoesServlet() {

			private static final long serialVersionUID = -1207374138713223126L;

			@HateoesResource(value = "{id}", consumes = JsonParser.class, produces = JsonRenderer.class)
			ReferenceList referenceList;
		};
		servlet.init();
		return servlet;
	}

	private static HateoesServlet createInMemServlet(final ServletConfig config) throws ServletException {
		final HateoesServlet servlet = new HateoesServlet();
		servlet.init(config);
		return servlet;
	}

}
