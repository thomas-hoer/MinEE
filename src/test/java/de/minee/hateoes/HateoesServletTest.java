package de.minee.hateoes;

import javax.servlet.ServletException;

import org.junit.Test;

import de.minee.datamodel.ReferenceList;
import de.minee.jpa.DAOImpl;

public class HateoesServletTest extends HateoesServlet {

	private static final long serialVersionUID = -6669308238856259151L;

	@HateoesResource("rlist/{id}/")
	@Persistent
	ReferenceList referenceList;

	@DataAccessObject
	DAOImpl daoImpl;

	@Test
	public void test() throws ServletException {
		init();
	}
}
