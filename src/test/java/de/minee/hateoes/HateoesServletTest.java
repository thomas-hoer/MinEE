package de.minee.hateoes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.minee.datamodel.ReferenceList;
import de.minee.jpa.DAOImpl;

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
	public void testRoot() throws ServletException, IOException {
		final MockHttpServletRequestImpl request = new MockHttpServletRequestImpl();
		final MockHttpServletResponseImpl response = new MockHttpServletResponseImpl();
		hateoesTestServlet.service(request, response);

		final String output = response.getWrittenOutput();
		assertNotNull(output);
		assertTrue(output.contains("rlist/id"));
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
		final HateoesServlet hateoesServlet = new HateoesServlet() {
			private static final long serialVersionUID = -6669308238856259151L;

			@HateoesResource("rlists")
			ReferenceList referenceLists;

		};
		hateoesServlet.init(); // should work fine

		MockHttpServletRequestImpl request;
		MockHttpServletResponseImpl response;
		String output;

		// Check for empty Database
		request = new MockHttpServletRequestImpl("rlists");
		response = new MockHttpServletResponseImpl();
		hateoesServlet.service(request, response);
		output = response.getWrittenOutput();
		assertNotNull(output);
		assertFalse(output.contains("ReferenceList"));

		// Insert new Element
		request = new MockHttpServletRequestImpl("rlists/create", Operation.POST, "");
		response = new MockHttpServletResponseImpl();
		hateoesServlet.service(request, response);
		output = response.getWrittenOutput();
		assertNotNull(output);
		assertTrue(output.contains("Success"));
		assertTrue(output.contains("New ID"));

		// Check again all entries of ReferenceList in Database
		request = new MockHttpServletRequestImpl("rlists");
		response = new MockHttpServletResponseImpl();
		hateoesServlet.service(request, response);
		output = response.getWrittenOutput();
		assertNotNull(output);
		assertTrue(output.contains("ReferenceList"));

	}
}
