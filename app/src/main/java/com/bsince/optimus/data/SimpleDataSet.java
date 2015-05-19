package com.bsince.optimus.data;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicNameValuePair;

import com.bsince.optimus.client.respon.ResponseHandler;
import com.bsince.optimus.event.Event;

public class SimpleDataSet extends AbstractDataSet {

	protected final Map<String, Object> keyValuePairs = new HashMap<String, Object>();
	
	//是否存放有集合类数据。
	protected boolean composite = false;

	public SimpleDataSet(String url) {
		super(url);
	}
	public SimpleDataSet(int method, String url) {
		
		super(method, url);
	}
	

	//这里当然可以全都用UrlEncodedFormEntity，但开发中，出现一个键对应一组值的情况毕竟较少，普通情况下时没这个必要来遍历判断
	@Override
	public HttpEntity getHttpEntity(Event<?> mEvent,ResponseHandler handler) {

		if(composite){
			try {
				return new UrlEncodedFormEntity(convertParameters(null, keyValuePairs), charSet);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}else{
			try {
				return new ByteArrayEntity(getDatasetString().getBytes(charSet));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public void put(Object... keysAndValues) {
        int len = keysAndValues.length;
        if (len % 2 != 0)
            throw new IllegalArgumentException("Supplied arguments must be even");
        for (int i = 0; i < len; i += 2) {
            String key = String.valueOf(keysAndValues[i]);
            String val = String.valueOf(keysAndValues[i + 1]);
            put(key, val);
        }
    }

	
	public void put(String key, Object value) {
		if(!composite){
			if(value instanceof Map ||value instanceof List||value instanceof Object[]||value instanceof Set){
				composite = true;
			}
		}
		keyValuePairs.put(key, value);
	}

	
	protected List<BasicNameValuePair> convertParameters(String key, Object value) {

		List<BasicNameValuePair> keyValuePairs = new LinkedList<BasicNameValuePair>();

		if (value instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) value;

			if (map.size() > 0) {
				for (Entry<?, ?> entry : map.entrySet()) {
					if (entry.getKey() instanceof String) {

						Object val = entry.getValue();

						if (val != null) {
							keyValuePairs.addAll(convertParameters(
									key == null ? (String) entry.getKey()
											: String.format(Locale.US,
													"%s[%s]", key, val), val));
						}

					}
				}
			}

		} else if (value instanceof List) {

			List<?> list = (List<?>) value;
			int listSize = list.size();
			for (int nestedValueIndex = 0; nestedValueIndex < listSize; nestedValueIndex++) {
				keyValuePairs.addAll(convertParameters(String.format(Locale.US,
						"%s[%d]", key, nestedValueIndex), list
						.get(nestedValueIndex)));
			}

		} else if (value instanceof Object[]) {
			Object[] array = (Object[]) value;
			int arrayLength = array.length;
			for (int nestedValueIndex = 0; nestedValueIndex < arrayLength; nestedValueIndex++) {
				keyValuePairs.addAll(convertParameters(String.format(Locale.US,
						"%s[%d]", key, nestedValueIndex),
						array[nestedValueIndex]));
			}
		} else if (value instanceof Set) {
			Set<?> set = (Set<?>) value;
			for (Object nestedValue : set) {
				keyValuePairs.addAll(convertParameters(key, nestedValue));
			}
		} else {
			keyValuePairs.add(new BasicNameValuePair(key, value.toString()));
		}
		return keyValuePairs;
	}
	@Override
	public String getDatasetString() throws UnsupportedEncodingException {
		StringBuilder encodedParams = new StringBuilder();

		for (Entry<String, Object> entry : keyValuePairs.entrySet()) {
			encodedParams.append(URLEncoder.encode(entry.getKey(),
					charSet));
			encodedParams.append('=');
			encodedParams.append(URLEncoder.encode(entry.getValue().toString(),
					charSet));
			encodedParams.append('&');
		}
		return encodedParams.toString();
	}

}
