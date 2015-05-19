package com.bsince.optimus.event;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.bsince.optimus.cache.HttpCache;
import com.bsince.optimus.cache.HttpCache.Entry;
import com.bsince.optimus.callback.LoadingProcessor;
import com.bsince.optimus.callback.Processor;
import com.bsince.optimus.client.req.Method;
import com.bsince.optimus.client.respon.OptHttpResponse;
import com.bsince.optimus.data.DataSet;
import com.bsince.optimus.data.SimpleDataSet;

public abstract class Event<T> {

	private int eventNo;

	private final DataSet dataSet;

	private final Processor<T> processor;
	
	public final LoadingProcessor loadingProcessor;

	private Object target;

	private AtomicBoolean cancel = new AtomicBoolean(false);

	private boolean cache = true;

	private boolean isReceiveResult;

	private boolean isFinish = false;
	
	private Entry entry;
	
	
	

	public final boolean isFinish() {
		return isFinish;
	}

	public final void markFinish(String howfinish) {
		this.isFinish = true;
		
	}

	public   boolean isCancel() {
		return cancel.get();
	}

	public final   void cancel() {
		this.cancel.set(true);
	}

	public final boolean isCache() {
		return cache;
	}

	public final void setCache(boolean cache) {
		this.cache = cache;
	}

	public Event(String url, Processor<T> processor) {
		this(new SimpleDataSet(url), processor,null);
	}

	public Event(DataSet set, Processor<T> processor){
		this(set,processor,null);
	}
	
	public Event(DataSet set, Processor<T> processor,LoadingProcessor loadingProcessor) {
		this.dataSet = set;
		this.processor = processor;
		this.loadingProcessor = loadingProcessor;
		setEventCacheDefault();
	}
	
	protected final void setEventCacheDefault(){
		switch (this.dataSet.getMethod()) {
		case Method.DELETE:
		case Method.PUT:
		case Method.POST:
		case Method.PATCH:
			setCache(false);
			break;

		default:
			setCache(true);
			break;
		}
	}

	public final int getEventNo() {
		return eventNo;
	}

	public final void setEventNo(int eventNo) {
		this.eventNo = eventNo;
	}

	public final DataSet getDataSet() {
		return dataSet;
	}

	public final Processor<T> getProcessor() {
		return processor;
	}

	public abstract OptHttpResponse<T> parse(byte [] data,Map<String,String> responseHeaders) ;

	public final Object getTarget() {
		return this.target;
	};

	public final void setTarget(Object target) {
		this.target = target;
	}

	// 标记为已接收到响应结果
	public final void markReceiveResult() {
		this.isReceiveResult = true;
	}

	// 是否已收到响应结果
	public final boolean hasHadResponseDelivered() {
		return isReceiveResult;
	}
	
	public  String getCacheKey() {
		return dataSet.getUrl();
	}
	
	public final boolean shoudGc(){
		return isCancel()||isFinish();
	}

	public final Entry getCacheEntry() {
		return entry;
	}

	public final void setCacheEntry(Entry entry) {
		this.entry = entry;
	}

}
