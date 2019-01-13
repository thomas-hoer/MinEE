package de.minee.cdi.beans;

import de.minee.cdi.Stateless;

public class SimpleBean {

	@Stateless
	NoReferenceBean noReferenceBean;

	public int increment(final int a) {
		return noReferenceBean.add(a, 1);
	}

}
