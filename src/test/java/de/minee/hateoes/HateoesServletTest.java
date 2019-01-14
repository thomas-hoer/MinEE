package de.minee.hateoes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import de.minee.datamodel.ReferenceList;
import de.minee.jpa.DAOImpl;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.junit.BeforeClass;
import org.junit.Test;

public class HateoesServletTest {
	private static HateoesServlet hateoesTestServlet = new HateoesServlet() {
		private static final long serialVersionUID = -6669308238856259151L;

		@HateoesResource("rlist/{id}")
		@Persistent
		ReferenceList referenceList;

		@DataAccessObject
		DAOImpl daoImpl;
	};

	@BeforeClass
	public static void initServlet() throws ServletException {
		hateoesTestServlet.init();

	}

	@Test
	public void testRoot() throws IOException {
		final MockHttpServletRequestImpl request = new MockHttpServletRequestImpl();
		final MockHttpServletResponseImpl response = new MockHttpServletResponseImpl();
		hateoesTestServlet.service(request, response);

		final String output = response.getWrittenOutput();
		assertNotNull(output);
		assertTrue(output.contains("rlist/{id}"));
	}

	@Test(expected = HateoesException.class)
	public void testInitPersistentWithoutDAO() throws ServletException {
		final HateoesServlet hateoesServlet = new HateoesServlet() {
			private static final long serialVersionUID = -6669308238856259151L;

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

		// Check again all entries of ReferenceList in Database
		request = new MockHttpServletRequestImpl("rlists");
		response = new MockHttpServletResponseImpl();
		hateoesServlet.service(request, response);
		final String outputCheckResourceIsPersisted = response.getWrittenOutput();

		assertNotNull(outputCheckEmpty);
		assertFalse(outputCheckEmpty.contains("ReferenceList"));

		assertNotNull(outputPostResource);
		assertTrue(outputPostResource.contains("Success"));
		assertTrue(outputPostResource.contains("New ID"));

		assertNotNull(outputCheckResourceIsPersisted);
		assertTrue(outputCheckResourceIsPersisted.contains("ReferenceList"));
		// Two elements found
		assertEquals(3, outputCheckResourceIsPersisted.split("ReferenceList").length);

		final UUID id = UUID.fromString(outputPostResource.split(":")[1]);
		request = new MockHttpServletRequestImpl("rlist/" + id.toString());
		response = new MockHttpServletResponseImpl();
		hateoesServlet.service(request, response);
		final String selectOutput = response.getWrittenOutput();

		assertNotNull(selectOutput);
		// One element found
		assertEquals(2, selectOutput.split("ReferenceList").length);
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
	public void testGetEdit() throws IOException, ServletException {
		final HateoesServlet hateoesServlet = createInMemServlet();
		MockHttpServletRequestImpl request;
		MockHttpServletResponseImpl response;
		request = new MockHttpServletRequestImpl("rlists/create", Operation.POST, "");
		response = new MockHttpServletResponseImpl();
		hateoesServlet.service(request, response);
		final UUID id = UUID.fromString(response.getWrittenOutput().split(":")[1]);

		request = new MockHttpServletRequestImpl("rlist/" + id + "/edit");
		response = new MockHttpServletResponseImpl();
		hateoesServlet.service(request, response);
		final String output = response.getWrittenOutput();
		assertNotNull(output);
		assertTrue(output.contains(id.toString()));
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

	private static HateoesServlet createInMemServlet() throws ServletException {
		final HateoesServlet servlet = new HateoesServlet() {
			private static final long serialVersionUID = -6669308238856259151L;

			@HateoesResource("rlists")
			ReferenceList referenceLists;

			@HateoesResource("rlist/{id}/")
			ReferenceList referenceList;
		};
		servlet.init();
		return servlet;
	}
}
