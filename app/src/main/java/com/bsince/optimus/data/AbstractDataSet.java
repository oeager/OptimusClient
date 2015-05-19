package com.bsince.optimus.data;

import org.apache.http.Header;

import com.bsince.optimus.client.req.Method;

public abstract class AbstractDataSet implements DataSet {

	protected int method;

	protected  String url;
	
	protected String charSet;

	protected String contentType;

	protected Header[] headers;

	public AbstractDataSet(String url) {
		
		this(Method.GET, url);
	}

	public AbstractDataSet(int method, String url) {
		this.url = url;
		this.method = method;
		init();
	}

	protected void init() {
		setContentType(Constants.DEFAULT_CONTENT);
		setCharSet(Constants.DEFAULT_CHARSET);

	}

	public void setUrl(String url) {
		this.url = url;
	}


	public void setCharSet(String charSet) {
		this.charSet = charSet;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setHeaders(Header[] headers) {
		this.headers = headers;
	}

	public void setMethod(int method) {

		this.method = method;
	}

	@Override
	public int getMethod() {
		return method;
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public String getCharset() {
		return charSet;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	public String getBodyContentType() {
		return contentType + "; charset=" + charSet;
	}

	@Override
	public Header[] getHeaders() {
		return headers;
	}

}
