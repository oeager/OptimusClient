package com.bsince.optimus.event;

import com.bsince.optimus.callback.LoadingProcessor;
import com.bsince.optimus.callback.Processor;
import com.bsince.optimus.data.DataSet;

public  class TextEvent extends SimpleEvent<String> {

	public TextEvent(String url, Processor<String> processor) {
		super(url, processor);
	}
	public TextEvent(DataSet set, Processor<String> processor) {
		super(set, processor);
	}
	public TextEvent(DataSet set, Processor<String> processor,LoadingProcessor loadingProcessor) {
		super(set,processor,loadingProcessor);
	}
	
	@Override
	public String parseToJavaBean(String data) throws Exception {
		return data;
	}
	
	

}
