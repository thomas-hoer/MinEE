package de.minee.hateoes.path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.minee.datamodel.ReferenceChain;
import de.minee.datamodel.SimpleReference;
import de.minee.hateoes.HateoesResource;
import de.minee.hateoes.HateoesServlet;
import de.minee.hateoes.MockHttpServletRequestImpl;
import de.minee.hateoes.MockHttpServletResponseImpl;
import de.minee.hateoes.Operation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.junit.BeforeClass;
import org.junit.Test;

public class JoinPathPartTest {

	private static HateoesServlet HATEOES_TEST_SERVLET = new HateoesServlet() {

		private static final long serialVersionUID = -397502535215997545L;

		@HateoesResource("rChain")
		ReferenceChain referenceChain;

		@HateoesResource("sRef")
		SimpleReference simpleReference;

		// Returns all ReferenceChain that are referencing a SimpleReference with
		// specific ids
		@HateoesResource("sRef/{simpleReference.id}/rChain/{id}")
		ReferenceChain forwardReference;

		// Returns all ReferenceChain that are referenced by SimpleReference by given id
		@HateoesResource("sRef/{SimpleReference.id\\referenceChain}")
		ReferenceChain backwardReference;

	};

	@BeforeClass
	public static void initServlet() throws ServletException {
		HATEOES_TEST_SERVLET.init();
	}

	@Test
	public void testForwardReference() throws ServletException, IOException {
		MockHttpServletRequestImpl request;
		MockHttpServletResponseImpl response;
		final UUID sRefId = UUID.randomUUID();
		request = new MockHttpServletRequestImpl("sRef/create", Operation.POST, null);
		request.addParameter("id", sRefId.toString());
		response = new MockHttpServletResponseImpl();
		HATEOES_TEST_SERVLET.service(request, response);

		request = new MockHttpServletRequestImpl("rChain/create", Operation.POST, null);
		final Map<String, String> parameters = new HashMap<>();
		final UUID rChainId = UUID.randomUUID();
		parameters.put("id", rChainId.toString());
		parameters.put("name", "Name?");
		parameters.put("simpleReference", sRefId.toString());
		request.addParameters(parameters);
		response = new MockHttpServletResponseImpl();
		HATEOES_TEST_SERVLET.service(request, response);
		assertEquals(HttpServletResponse.SC_OK, response.getError());

		request = new MockHttpServletRequestImpl("sRef/" + sRefId + "/rChain/" + rChainId);
		response = new MockHttpServletResponseImpl();
		HATEOES_TEST_SERVLET.service(request, response);
		final String output = response.getWrittenOutput();
		assertTrue(output.contains("SimpleReference"));
		assertTrue(output.contains("ReferenceChain"));
		assertTrue(output.contains("Name?"));
	}

	@Test
	public void testBackwardReference() throws ServletException, IOException {
		MockHttpServletRequestImpl request;
		MockHttpServletResponseImpl response;
		final UUID rChainId = UUID.randomUUID();
		request = new MockHttpServletRequestImpl("rChain/create", Operation.POST, null);
		request.addParameter("id", rChainId.toString());
		request.addParameter("name", "rChainName");
		response = new MockHttpServletResponseImpl();
		HATEOES_TEST_SERVLET.service(request, response);

		request = new MockHttpServletRequestImpl("sRef/create", Operation.POST, null);
		final Map<String, String> parameters = new HashMap<>();
		final UUID sRefId = UUID.randomUUID();
		parameters.put("id", sRefId.toString());
		parameters.put("name", "sRefName");
		parameters.put("referenceChain", rChainId.toString());
		request.addParameters(parameters);
		response = new MockHttpServletResponseImpl();
		HATEOES_TEST_SERVLET.service(request, response);
		assertEquals(HttpServletResponse.SC_OK, response.getError());

		request = new MockHttpServletRequestImpl("sRef/" + sRefId);
		response = new MockHttpServletResponseImpl();
		HATEOES_TEST_SERVLET.service(request, response);
		final String output = response.getWrittenOutput();
		assertFalse(output.contains("SimpleReference"));
		assertTrue(output.contains("ReferenceChain"));
		assertFalse(output.contains("sRefName"));
		assertTrue(output.contains("rChainName"));
	}
}
