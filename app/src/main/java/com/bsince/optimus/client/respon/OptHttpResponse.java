package com.bsince.optimus.client.respon;

import com.bsince.optimus.cache.HttpCache;


public class OptHttpResponse<T> {

	public static <T> OptHttpResponse<T> success(T result, HttpCache.Entry cacheEntry) {
		return new OptHttpResponse<T>(result, cacheEntry);
	}

	public static <T> OptHttpResponse<T> error(Exception error) {
		return new OptHttpResponse<T>(error);
	}

	public final T result;

	public final HttpCache.Entry cacheEntry;

	public final Exception error;

	public boolean intermediate = false;
	

	private OptHttpResponse(T result, HttpCache.Entry cacheEntry) {
		this.result = result;
		this.cacheEntry = cacheEntry;
		this.error = null;
	}

	private OptHttpResponse(Exception error) {
		this.result = null;
		this.cacheEntry = null;
		this.error = error;
	}
}
