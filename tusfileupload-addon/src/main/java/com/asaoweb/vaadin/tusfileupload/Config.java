package com.asaoweb.vaadin.tusfileupload;

import java.io.File;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asaoweb.vaadin.tusfileupload.exceptions.TusException;

public class Config
{
	private static final Logger log = LoggerFactory.getLogger(Config.class.getName());

	String tusApiVersionSupported  = "1.0.0";

	String[] tusApiExtensions = {"creation", "termination"};

	//Maximum size of a single upload. 0 means unlimited.
	long MAX_SIZE = 0L;

	//Maximum total storage space to use.  0 means unlimited (or different policy in use). 
	long MAX_STORAGE = 0L;

	//Server can limit number of bytes it will accept in a single patch request.  0 means unlimited.
	long MAX_REQUEST = 0L;

	// Default value. Folder where data will be stored.  Servlet will create the folder if it doesn't exist and
	// parent dir does exist and permissions allow.  
	String UPLOAD_FOLDER = "/tmp";

	public long maxSize;
	public long maxStorage;
	public long maxRequest;
	public String uploadFolder;
	public String datastoreProvider;

	// Derived classes may need additional configuration.
	public Properties allProperties = new Properties(); 


    public Config() throws TusException.ConfigError
    {	
		Properties properties = new Properties();
		properties.setProperty("uploadFolder", "/tmp");
        properties.setProperty("maxFileSize", "0");
		properties.setProperty("maxStorage", "0");
		properties.setProperty("maxRequest", "0");
		//properties.setProperty("datastoreProvider", null);
		
		init(properties);
   	}

	public Config(Properties properties) throws TusException.ConfigError
	{
		init(properties);
	}

	private void init(Properties properties) throws TusException.ConfigError 
	{
		String tmp;
		Long l; 

		this.allProperties.putAll(properties);

		tmp = properties.getProperty("uploadFolder");
		uploadFolder = (tmp == null)? UPLOAD_FOLDER : tmp;
		validateFolder(uploadFolder);

		l = getLongValue("maxFileSize");
		maxSize = (l == null) ? MAX_SIZE : l;

		l = getLongValue("maxStorage");
		maxStorage = (l == null) ? MAX_STORAGE : l;

		l = getLongValue("maxRequest");
		maxRequest = (l == null) ? MAX_REQUEST : l;

		datastoreProvider = properties.getProperty("datastoreProvider");
		log.info("uploadFolder=" + uploadFolder + ", maxFileSize=" + maxSize + ", maxStorage=" + maxStorage +
			", maxRequest=" + maxRequest + ", datastoreProvider=" + datastoreProvider);
	}


 
	
	protected void validateFolder(String folder) throws TusException.ConfigError
	{
		String tmp;
		File file = new File(folder);
		if (!file.isDirectory() && !file.mkdir())
		{
			tmp = "Unable to find or create directory " + folder;
			log.error(tmp);
			throw new TusException.ConfigError(tmp);
		}
		if (!file.canWrite() || !file.canRead())
		{
			tmp = "Upload directory: " + folder + " must be readable and writable";
			log.error(tmp);
			throw new TusException.ConfigError(tmp);
		}
	}

	public String getStringValue(String name)
	{
		return allProperties.getProperty(name);
	}

	public Long getLongValue(String name) throws TusException.ConfigError
	{
		String msg;
		String value = allProperties.getProperty(name);
		if (value == null)
		{
			return null;
		}
		Long longValue = null;
		try
		{
			longValue = new  Long(value);
			return longValue;
		}
		catch(NumberFormatException ne)
		{
			msg = "Parameter must be a long.  Error parsing: " + value;
			throw new TusException.ConfigError(msg);
		}
	}
}