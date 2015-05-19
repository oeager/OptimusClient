package com.bsince.optimus.client.respon;

import android.os.Handler;
import android.os.Looper;

import com.bsince.optimus.event.Event;
import com.bsince.optimus.utils.L;

public class ResponseHandler {

	private final Handler handler;

	public ResponseHandler() {
		this.handler = new Handler(Looper.getMainLooper());
	}

	public ResponseHandler(Handler handler) {
		this.handler = handler;
	}
	
	public void postPreExecute(final Event<?> mEvent){
		if(Looper.myLooper()==Looper.getMainLooper()){
			mEvent.getProcessor().onPreExecute();
		}else{
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					mEvent.getProcessor().onPreExecute();
				}
			});
		}
	}
	
	public <T> void responSucess(Event<T> event,int statusCode,OptHttpResponse<T> result){
		responSucess(event,statusCode, result,null);
	}
	public <T> void responSucess(Event<T> event,int statusCode,OptHttpResponse<T> result,Runnable runnable){
		event.markReceiveResult();
		handler.post(new ResponseRunnable<T>(event,statusCode, result, runnable)) ;
	}
	
	public <T> void responFail(Event<T> event,int statusCode,Exception e){
		OptHttpResponse<T> response = OptHttpResponse.error(e);
		handler.post(new ResponseRunnable<T>(event, statusCode,response, null));
	}
	
	public void responProcessing(final Event<?> event,final long currentSize,final long total){
		
		if(total==0){
			return ;
		}
		if(event.loadingProcessor.isComplete()){
			return ;
		}
		
		final int percent =(int)(((float)currentSize)/total*100);
		
		if( (percent - event.loadingProcessor.getCurrentPercent())>=100/event.loadingProcessor.getTotalPostTimes()){
			if(event.loadingProcessor.shouldProssingOnUIThread()){
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						event.loadingProcessor.onProcessing(percent,currentSize, total);
					}
				});
			}else{
				event.loadingProcessor.onProcessing(percent ,currentSize, total);
			}
			event.loadingProcessor.setCurrentPercent(percent);
		};
	}

	private static class ResponseRunnable<T> implements Runnable {

		private final Event<T> mEvent;
		private final OptHttpResponse<T> mResponseResult;
		private final Runnable mRunnable;
		private final int statusCode;

		public ResponseRunnable(Event<T> mEvent,int statusCode, OptHttpResponse<T> response,Runnable runnable) {
			this.mEvent = mEvent;
			mResponseResult = response;
			mRunnable = runnable;
			this.statusCode = statusCode;
		}

		@Override
		public void run() {

			if (mEvent.isCancel()) {
				mEvent.markFinish("canceled-at-delivery");
				return;
			}

			mEvent.getProcessor().onPostExecute(statusCode,mResponseResult.result, mResponseResult.error);
		
			if (mResponseResult.intermediate) {
				L.d("intermediate-response");
			} else {
				mEvent.markFinish("done");
			}

			if (mRunnable != null) {
				mRunnable.run();
			}

		}

	}
}
