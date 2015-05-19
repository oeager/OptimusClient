package com.bsince.optimus.callback;


public interface Processor<T> {

	void onPreExecute();
	
	
	
	void onPostExecute(int statusCode, T t, Exception e);
	
}
