package com.bsince.optimus.callback;


public abstract class BaseProcessor<T> implements Processor<T> {
	@Override
	public void onPreExecute() {
		
	}


	@Override
	public void onPostExecute(int statusCode,T t, Exception e) {
		if(e!=null){
			onFail(statusCode,e);
		}else{
			onSuccess(statusCode,t);
		}
	}
	
	public abstract void onSuccess(int statusCode,T t);
	
	public abstract void onFail(int statusCode,Exception e);

}
