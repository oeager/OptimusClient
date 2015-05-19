package com.bsince.optimus.data;

import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import com.bsince.optimus.client.respon.ResponseHandler;
import com.bsince.optimus.event.Event;

public class JsonDataSet extends AbstractDataSet {

	private String requestBody;
	
	public JsonDataSet(String url) {
		super(url);
	}
	
	public JsonDataSet(int method, String url) {
		
		super(method, url);
	}
	
	@Override
	protected void init() {
		setCharSet(Constants.DEFAULT_CHARSET);
		setContentType(Constants.JSON_CONTENT);
	}
	
	public void setRequestBody(JSONObject object){
		this.requestBody = object.toString();
	}

	public void setRequestBody(JSONArray object){
		this.requestBody = object.toString();
	}

	@Override
	public HttpEntity getHttpEntity(Event<?> mEvent,ResponseHandler handler) {
		if(this.requestBody!=null){
			try {
				return new ByteArrayEntity(requestBody.getBytes(charSet));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public String getDatasetString() throws UnsupportedEncodingException {
		return requestBody;
	}

	
}
