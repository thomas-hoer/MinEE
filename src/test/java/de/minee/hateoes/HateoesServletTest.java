package de.minee.hateoes;

import javax.servlet.ServletException;

import org.junit.Test;

import de.minee.datamodel.Gallery;
import de.minee.hateoes.DataAccessObject;
import de.minee.hateoes.HateoesResource;
import de.minee.hateoes.HateoesServlet;
import de.minee.hateoes.Persistent;
import de.minee.jpa.DAOImpl;

public class HateoesServletTest extends HateoesServlet {

	private static final long serialVersionUID = -6669308238856259151L;

	@HateoesResource("gallery/{id}/")
	@Persistent
	Gallery gallery;

	@DataAccessObject
	DAOImpl daoImpl;

	@Test
	public void test() throws ServletException {
		init();
	}
}
