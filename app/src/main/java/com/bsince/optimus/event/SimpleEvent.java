package com.bsince.optimus.event;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import com.bsince.optimus.callback.LoadingProcessor;
import com.bsince.optimus.callback.Processor;
import com.bsince.optimus.client.respon.OptHttpResponse;
import com.bsince.optimus.data.DataSet;
import com.bsince.optimus.utils.HeaderUtils;

public abstract class SimpleEvent <T>  extends Event<T> {

	public SimpleEvent(String url, Processor<T> processor) {
		super(url, processor);
	}

	public SimpleEvent(DataSet set, Processor<T> processor) {
		super(set, processor);
	}
	
	public SimpleEvent(DataSet set, Processor<T> processor,LoadingProcessor loadingProcessor) {
		super(set,processor,loadingProcessor);
	}
	
	@Override
	public  OptHttpResponse<T> parse(byte [] data,
			Map<String, String> responseHeaders)  {
		String parsed;
		try {

			parsed = new String(data,
					HeaderUtils.parseCharset(responseHeaders));
		} catch (UnsupportedEncodingException e) {
			parsed = new String(data);
		}
		
		try {
			return OptHttpResponse.success(parseToJavaBean(parsed), HeaderUtils.parseCacheHeaders(responseHeaders, data));
		} catch (Exception e) {
			
			return OptHttpResponse.error(e);
		}
	}
	public abstract T parseToJavaBean(String data) throws Exception;

}
