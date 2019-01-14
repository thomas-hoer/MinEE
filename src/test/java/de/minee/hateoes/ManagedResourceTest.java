package de.minee.hateoes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.minee.datamodel.SimpleReference;
import de.minee.jpa.DAOImpl;

import org.junit.BeforeClass;
import org.junit.Test;

public class ManagedResourceTest {

	private static final ManagedResource<SimpleReference> RESOURCE = new ManagedResource<>("root",
			new Operation[] { Operation.ALL }, SimpleReference.class);

	@BeforeClass
	public static void prepare() {
		RESOURCE.setDao(new DAOImpl());
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
		final ManagedResource<SimpleReference> resource = new ManagedResource<>("root",
				new Operation[] { Operation.GET }, SimpleReference.class);
		assertTrue(resource.isMethodAllowed(Operation.GET.name()));
		assertFalse(resource.isMethodAllowed(Operation.POST.name()));
		assertFalse(resource.isMethodAllowed(Operation.PUT.name()));
		assertFalse(resource.isMethodAllowed(Operation.DELETE.name()));
	}

	@Test
	public void testMethodAllowedPostDelete() {
		final ManagedResource<SimpleReference> resource = new ManagedResource<>("root",
				new Operation[] { Operation.POST, Operation.DELETE }, SimpleReference.class);
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
