package de.minee.cdi;

import de.minee.datamodel.SimpleReference;

import javax.servlet.ServletException;

import org.junit.Assert;
import org.junit.Test;

public class CdiAwareHttpServletTest {

	@Test
	public void testInit() throws ServletException {
		final TestServlet servlet = new TestServlet();
		servlet.init();
		Assert.assertNotNull(servlet.getSimpleReference());
	}

	@Test
	public void testInitServletConfig() throws ServletException {
		final TestServlet servlet = new TestServlet();
		servlet.init(null);
		Assert.assertNotNull(servlet.getSimpleReference());
	}

	private class TestServlet extends CdiAwareHttpServlet {
		private static final long serialVersionUID = 1L;
		@Stateless
		private SimpleReference simpleReference;

		SimpleReference getSimpleReference() {
			return simpleReference;
		}
	}

}
