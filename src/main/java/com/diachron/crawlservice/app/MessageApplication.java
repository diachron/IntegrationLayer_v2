package com.diachron.crawlservice.app;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.diachron.crawlservice.rest.DiacrawlRestService;

public class MessageApplication extends Application {
	private Set<Object> singletons = new HashSet<Object>();

	public MessageApplication() {
		singletons.add(new DiacrawlRestService());
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}
}
