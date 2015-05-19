package com.bsince.optimus.client.interceptor;

import java.io.IOException;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

import com.bsince.optimus.data.Constants;

public class GzipBothClientHeadersRequestInterceptor implements HttpRequestInterceptor {

	private Map<String,String> headers;
	
	public GzipBothClientHeadersRequestInterceptor(Map<String,String> headers){
		this.headers = headers;
	}
	
	@Override
	public void process(HttpRequest request, HttpContext context)
			throws HttpException, IOException {

		if (!request.containsHeader(Constants.HEADER_ACCEPT_ENCODING)) {
			request.addHeader(Constants.HEADER_ACCEPT_ENCODING,
					Constants.ENCODING_GZIP);
		}
		if (headers != null) {
			for (String header : headers.keySet()) {
				if (request.containsHeader(header)) {
					Header overwritten = request.getFirstHeader(header);
					request.removeHeader(overwritten);
				}
				request.addHeader(header, headers.get(header));
			}
		}

	
	}

}
