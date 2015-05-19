package com.bsince.optimus.client.interceptor;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;

import com.bsince.optimus.client.OptimusClient.InflatingEntity;
import com.bsince.optimus.data.Constants;

public class GzipResponseInterceptor implements HttpResponseInterceptor {

	@Override
	public void process(HttpResponse response, HttpContext context)
			throws HttpException, IOException {

		final HttpEntity entity = response.getEntity();
		if (entity == null) {
			return;
		}
		final Header encoding = entity.getContentEncoding();
		if (encoding != null) {
			for (HeaderElement element : encoding.getElements()) {
				if (element.getName().equalsIgnoreCase(
						Constants.ENCODING_GZIP)) {
					response.setEntity(new InflatingEntity(entity));
					break;
				}
			}
		}
	
	}

}
