package com.asaoweb.vaadin.tusfileupload.handlers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.VaadinResponse;

public class Response 
{
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(Response.class.getName());
	public static final int OK = HttpServletResponse.SC_OK;
	public static final int NO_CONTENT = HttpServletResponse.SC_NO_CONTENT;
	public static final int CREATED = HttpServletResponse.SC_CREATED;

	private String text = "";
	private int status;
	private Properties headers = new Properties();
	private VaadinResponse vaadinResponse;

	public Response(VaadinResponse vaadinResponse)
	{
		this.vaadinResponse = vaadinResponse;
	}

	public String getText() { return text; }
	public int getStatus() { return status; }
	public Properties getHeaders() { return headers; }

	public Response setText(String text)
	{
		this.text = text;
		return this;
	}

	public Response setStatus(int status)
	{
		this.status = status;
		return this;
	}

	public Response setHeader(String name, String value)
	{
		headers.setProperty(name, value);
		return this;
	}
	
	public void write() throws IOException
	{
		vaadinResponse.setStatus(this.status);
		for (String name : headers.stringPropertyNames())
		{
			vaadinResponse.setHeader(name, headers.getProperty(name));
		}
		PrintWriter out = vaadinResponse.getWriter();
		out.print(this.text);
	}

}
