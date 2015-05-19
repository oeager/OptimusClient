package com.bsince.optimus.data;

import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

import com.bsince.optimus.client.respon.ResponseHandler;
import com.bsince.optimus.event.Event;


public interface DataSet {
	
	int getMethod();
	
	void setUrl(String url);
	
	String getUrl();
	
	String getDatasetString() throws UnsupportedEncodingException;
	
	String getCharset();
	
	String getContentType();
	
	String getBodyContentType();

	Header [] getHeaders();

	HttpEntity getHttpEntity(Event<?> mEvent, ResponseHandler handler);
	
	

}
