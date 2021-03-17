package com.asaoweb.vaadin.tusfileupload;

import com.asaoweb.vaadin.tusfileupload.data.Datastore;
import com.asaoweb.vaadin.tusfileupload.data.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class Composer implements Serializable {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(Composer.class.getName());

	protected final Config config;
	protected final Datastore datastore;
	protected final Locker locker;


	public Composer(Config config) throws Exception
	{
		this.config = config;

		locker = new SingleProcessLocker();

		if (config.datastoreProvider != null)
		{
			datastore = (Datastore)Class.forName(config.datastoreProvider).getConstructor().newInstance();
		} else
		{
			datastore = new Store();
		}
		datastore.init(config, locker);
	}


	public Config getConfig() {
		return config;
	}


	public Datastore getDatastore() {
		return datastore;
	}


	public Locker getLocker() {
		return locker;
	}
}
