package org.javasimon.demoapp.web;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.javasimon.SimonManager;

public class PersonsPage extends WebPage {
	private static final long serialVersionUID = 1L;

	public PersonsPage(final PageParameters parameters) {
		SimonManager.getCounter("some.counter").increase(2);
		SimonManager.getCounter("some.counter").decrease();
	}
}
