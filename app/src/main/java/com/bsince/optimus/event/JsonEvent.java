package com.bsince.optimus.event;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bsince.optimus.callback.LoadingProcessor;
import com.bsince.optimus.callback.Processor;
import com.bsince.optimus.data.DataSet;

public abstract class JsonEvent<T> extends SimpleEvent<T> {

	public JsonEvent(String url, Processor<T> processor) {
		super(url, processor);
		
	}

	public JsonEvent(DataSet set, Processor<T> processor) {
		super(set, processor);
		
	}
	public JsonEvent(DataSet set, Processor<T> processor,LoadingProcessor loadingProcessor) {
		super(set,processor,loadingProcessor);
	}
	

	@Override
	public T parseToJavaBean(String data) throws Exception  {
		try {
			JSONObject jsonObject = new JSONObject(data);
			return parseJson(jsonObject,null);
		} catch (JSONException e) {
			try {
				JSONArray jsonArray = new JSONArray(data);
				return parseJson(null, jsonArray);
			} catch (JSONException e2) {
				throw e;
			}
		
		}
		
	}
	
	abstract T parseJson(JSONObject jo,JSONArray ja);

	
}
