package de.minee.cdi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import de.minee.cdi.beans.RecursiveBean;
import de.minee.cdi.beans.SimpleBean;
import de.minee.datamodel.SimpleReference;

import org.junit.Test;

public class CdiUtilTest {

	@Test
	public void testGetInstance() {
		final SimpleReference simpleReference = CdiUtil.getInstance(SimpleReference.class);
		assertNotNull(simpleReference);
		assertNull(simpleReference.getContent());
		assertNull(simpleReference.getId());
		assertNull(simpleReference.getName());
		assertNull(simpleReference.getReferenceChain());
		assertNull(simpleReference.getValue());
	}

	@Test
	public void testGetInstanceBean() {
		final SimpleBean simpleBean = CdiUtil.getInstance(SimpleBean.class);
		assertEquals(2, simpleBean.increment(1));
	}

	@Test(expected = NullPointerException.class)
	public void testBeanWithoutCdiUtil() {
		final SimpleBean simpleBean = new SimpleBean();
		simpleBean.increment(1);
	}

	@Test
	public void testGetInstanceRecursiveBean() {
		final RecursiveBean recursiveBean = CdiUtil.getInstance(RecursiveBean.class);
		assertNotNull(recursiveBean);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetInstanceNull() {
		CdiUtil.getInstance(null);
	}

	@Test(expected = CdiException.class)
	public void testCdiException() {
		CdiUtil.getInstance(NotInstanciatable.class);
	}

	private static class NotInstanciatable {
		private NotInstanciatable() {
			throw new RuntimeException();
		}
	}
}
