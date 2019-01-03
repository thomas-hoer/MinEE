package de.minee.cdi;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class CdiAwareHttpServlet extends HttpServlet {

	private static final long serialVersionUID = 3019639349886399459L;

	@Override
	public void init() throws ServletException {
		super.init();
		CdiUtil.injectResources(this);
	}

	@Override
	public void init(final ServletConfig config) throws ServletException {
		super.init(config);
		CdiUtil.injectResources(this);
	}

}
