package de.minee.cdi;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public abstract class CdiAwareHttpServlet extends HttpServlet {

	private static final long serialVersionUID = 3019639349886399459L;

	@Override
	public void init() throws ServletException {
		CdiUtil.injectResources(this);
	}

}
