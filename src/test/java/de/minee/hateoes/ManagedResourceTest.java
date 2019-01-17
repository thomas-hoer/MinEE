package de.minee.hateoes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.minee.datamodel.EnumObject;
import de.minee.datamodel.ReferenceList;
import de.minee.datamodel.SimpleReference;
import de.minee.jpa.AbstractStatement;
import de.minee.jpa.DAOTestImpl;
import de.minee.jpa.InitialQueryConnection;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.junit.BeforeClass;
import org.junit.Test;

public class ManagedResourceTest {

	private static final ManagedResource<SimpleReference> RESOURCE = new ManagedResource<>("root",
			new Operation[] { Operation.ALL }, SimpleReference.class);

	@BeforeClass
	public static void prepare() {
		RESOURCE.setDao(new DAOTestImpl());
	}

	@Test
	public void testServeNotAllowed() throws IOException {
		final MockHttpServletRequestImpl request = new MockHttpServletRequestImpl("root/foo");
		final MockHttpServletResponseImpl response = new MockHttpServletResponseImpl();
		RESOURCE.serve(request, response);
		assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, response.getError());
	}

	@Test
	public void testServeConnectionLost() throws IOException {
		final ManagedResource<EnumObject> resource = new ManagedResource<>("root", new Operation[] { Operation.ALL },
				EnumObject.class);
		resource.setDao(new DAOTestImpl() {
			@Override
			public <T> InitialQueryConnection<T, AbstractStatement<T>> select(final Class<T> clazz)
					throws SQLException {
				throw new SQLException();
			}
		});
		final MockHttpServletRequestImpl request = new MockHttpServletRequestImpl("root");
		final MockHttpServletResponseImpl response = new MockHttpServletResponseImpl();
		resource.serve(request, response);
		assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response.getError());
	}

	@Test(expected = HateoesException.class)
	public void testServeCreateException() throws IOException {
		final ManagedResource<SimpleReference> resource = new ManagedResource<>("root",
				new Operation[] { Operation.ALL }, SimpleReference.class);
		resource.setDao(new DAOTestImpl() {
			@Override
			public <T> InitialQueryConnection<T, AbstractStatement<T>> select(final Class<T> clazz)
					throws SQLException {
				throw new SQLException();
			}
		});
		final MockHttpServletRequestImpl request = new MockHttpServletRequestImpl("root/create", Operation.POST, "");
		final MockHttpServletResponseImpl response = new MockHttpServletResponseImpl();
		request.addParameter("referenceChain", UUID.randomUUID().toString());
		resource.serve(request, response);
		assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response.getError());
	}

	@Test
	public void testMethodAllowedAll() {
		assertTrue(RESOURCE.isMethodAllowed(Operation.GET.name()));
		assertTrue(RESOURCE.isMethodAllowed(Operation.POST.name()));
		assertTrue(RESOURCE.isMethodAllowed(Operation.PUT.name()));
		assertTrue(RESOURCE.isMethodAllowed(Operation.DELETE.name()));
		assertFalse(RESOURCE.isMethodAllowed(Operation.ALL.name()));
	}

	@Test
	public void testMethodAllowedGet() {
		final ManagedResource<EnumObject> resource = new ManagedResource<>("root", new Operation[] { Operation.GET },
				EnumObject.class);
		assertTrue(resource.isMethodAllowed(Operation.GET.name()));
		assertFalse(resource.isMethodAllowed(Operation.POST.name()));
		assertFalse(resource.isMethodAllowed(Operation.PUT.name()));
		assertFalse(resource.isMethodAllowed(Operation.DELETE.name()));
	}

	@Test
	public void testMethodAllowedPostDelete() {
		final ManagedResource<ReferenceList> resource = new ManagedResource<>("root",
				new Operation[] { Operation.POST, Operation.DELETE }, ReferenceList.class);
		assertFalse(resource.isMethodAllowed(Operation.GET.name()));
		assertTrue(resource.isMethodAllowed(Operation.POST.name()));
		assertFalse(resource.isMethodAllowed(Operation.PUT.name()));
		assertTrue(resource.isMethodAllowed(Operation.DELETE.name()));
	}

	@Test
	public void testIsMatch() {
		final boolean match = RESOURCE.isMatch("root");
		assertTrue(match);
	}

	@Test
	public void testIsMatchEdit() {
		final boolean match = RESOURCE.isMatch("root/edit");
		assertTrue(match);
	}

	@Test
	public void testIsMatchCreate() {
		final boolean match = RESOURCE.isMatch("root/create");
		assertTrue(match);
	}

	@Test
	public void testIsMatchFalse() {
		final boolean match = RESOURCE.isMatch("foo");
		assertFalse(match);
	}

	@Test
	public void testIsMatchFalse2() {
		final boolean match = RESOURCE.isMatch("root/foo");
		assertFalse(match);
	}
}
