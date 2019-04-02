package de.minee.util;

import de.minee.datamodel.SimpleReference;
import de.minee.jpa.SelectStatement;
import de.minee.util.ProxyFactory.ProxyException;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ProxyFactoryTest {

	@Test
	public void testGetProxy() throws ProxyException {
		final SimpleReference proxy = ProxyFactory.getProxy(SimpleReference.class);
		Assert.assertNotEquals(SimpleReference.class, proxy.getClass());
	}

	@Test(expected = RuntimeException.class)
	public void testGetProxyInterfaceComparable() throws ProxyException {
		ProxyFactory.getProxy(Comparable.class);
	}

	@Test(expected = RuntimeException.class)
	public void testGetProxyInterfaceList() throws ProxyException {
		ProxyFactory.getProxy(List.class);
	}

	@Test
	public void testGetProxyNull() throws ProxyException {
		final Object proxy = ProxyFactory.getProxy(null);
		Assert.assertNotNull(proxy);
	}

	@Test(expected = ProxyException.class)
	public void testGetProxyNoDefaultConstructor() throws ProxyException {
		ProxyFactory.getProxy(SelectStatement.class);
	}
}
