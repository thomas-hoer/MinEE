package de.minee.hateoes.path;

import static org.junit.Assert.assertEquals;

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

import org.junit.BeforeClass;
import org.junit.Test;

public class JoinPathPartTest {

	private static HateoesServlet HATEOES_TEST_SERVLET = new HateoesServlet() {

		private static final long serialVersionUID = -397502535215997545L;

		@HateoesResource("rChain")
		ReferenceChain referenceChain;

		@HateoesResource("sRef")
		SimpleReference simpleReference;

		@HateoesResource("rChain/{id}/sRef/{simpleReference.id}")
		ReferenceChain forwardReference;

		@HateoesResource("sRef/{SimpleReference.id\\referenceChain}")
		SimpleReference backwardReference;

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
		// assertEquals(HttpServletResponse.SC_OK, response.getError());

		request = new MockHttpServletRequestImpl("rChain/" + rChainId + "/sRef/" + sRefId);
		response = new MockHttpServletResponseImpl();
		HATEOES_TEST_SERVLET.service(request, response);
		assertEquals("", response.getWrittenOutput());
	}
}
