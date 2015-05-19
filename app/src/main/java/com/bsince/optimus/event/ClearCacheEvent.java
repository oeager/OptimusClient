package com.bsince.optimus.event;

import java.util.Map;

import android.os.Handler;
import android.os.Looper;

import com.bsince.optimus.cache.HttpCache;
import com.bsince.optimus.client.respon.OptHttpResponse;


public class ClearCacheEvent extends Event<Object> {

	
	private final HttpCache mCache;
	
	private final Runnable mCallback;

	public ClearCacheEvent(HttpCache cache, Runnable callback) {
		super("", null);
		mCache = cache;
		mCallback = callback;
	}

	@Override
	public boolean isCancel() {
		mCache.clear();
		if (mCallback != null) {
			Handler handler = new Handler(Looper.getMainLooper());
			handler.postAtFrontOfQueue(mCallback);
		}
		return true;
	}

	@Override
	public OptHttpResponse<Object> parse(byte[] data,
			Map<String, String> responseHeaders) {
		// TODO Auto-generated method stub
		return null;
	}


	

}
