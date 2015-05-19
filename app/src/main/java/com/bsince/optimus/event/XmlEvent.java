package com.bsince.optimus.event;

import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.bsince.optimus.callback.LoadingProcessor;
import com.bsince.optimus.callback.Processor;
import com.bsince.optimus.data.DataSet;

public abstract class XmlEvent<T> extends SimpleEvent<T> {

	public XmlEvent(String url, Processor<T> processor) {
		super(url, processor);
	}

	public XmlEvent(DataSet set, Processor<T> processor) {
		super(set, processor);
	}

	public XmlEvent(DataSet set, Processor<T> processor,LoadingProcessor loadingProcessor) {
		super(set,processor,loadingProcessor);
	}
	

	@Override
	public T parseToJavaBean(String data) throws Exception {
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		XmlPullParser xmlPullParser = factory.newPullParser();
		xmlPullParser.setInput(new StringReader(data));
		return parseToJavaBean(xmlPullParser);
	}
	abstract T parseToJavaBean(XmlPullParser data) throws Exception ;

}
