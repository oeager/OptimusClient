package com.bsince.optimus.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;

import com.bsince.optimus.client.req.Method;
import com.bsince.optimus.client.respon.ResponseHandler;
import com.bsince.optimus.data.MulitDataSet.FileWrapper;
import com.bsince.optimus.data.MulitDataSet.StreamWrapper;
import com.bsince.optimus.data.entity.JsonStreamerEntity;
import com.bsince.optimus.event.Event;

/**
 * 以json流的形式上传字符与流，采用了Gzip，更省内存与流量
 * @author oeager
 *
 */
public class JsonStreamDataSet extends AbstractDataSet {
	protected final Map<String, Object> keyValuePairs = new HashMap<String, Object>();
	
	protected final HashMap<String, StreamWrapper> streamParams = new HashMap<String, StreamWrapper>();
	
	protected final HashMap<String, FileWrapper> fileParams = new HashMap<String, FileWrapper>();
	
	protected String elapsedFieldInJsonStreamer = "_elapsed";

	public void setElapsedFieldInJsonStreamer(String elapsedFieldInJsonStreamer) {
		this.elapsedFieldInJsonStreamer = elapsedFieldInJsonStreamer;
	}

	public JsonStreamDataSet(String url) {
		this(Method.POST, url);
	}

	public JsonStreamDataSet(int method, String url) {
		super(method, url);
	}

	@Override
	public String getBodyContentType() {
		// just set it in the entity
		return null;
	}

	@Override
	public String getDatasetString() throws UnsupportedEncodingException {
		StringBuilder encodedParams = new StringBuilder();

		for (Map.Entry<String, Object> entry : keyValuePairs.entrySet()) {
			encodedParams.append(URLEncoder.encode(entry.getKey(),
					charSet));
			encodedParams.append('=');
			encodedParams.append(URLEncoder.encode(entry.getValue().toString(),
					charSet));
			encodedParams.append('&');
		}
		return encodedParams.toString();
	}

	@Override
	public HttpEntity getHttpEntity(Event<?> mEvent, ResponseHandler handler) {
		JsonStreamerEntity entity = new JsonStreamerEntity(mEvent,handler,
	            !fileParams.isEmpty() || !streamParams.isEmpty(),
	            elapsedFieldInJsonStreamer);
		 // Add non-string params
        for (Map.Entry<String, Object> entry : keyValuePairs.entrySet()) {
            entity.addPart(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, FileWrapper> entry : fileParams.entrySet()) {
            entity.addPart(entry.getKey(), entry.getValue());
        }

        // Add stream params
        for (Map.Entry<String, StreamWrapper> entry : streamParams.entrySet()) {
            StreamWrapper stream = entry.getValue();
            if (stream.inputStream != null) {
                entity.addPart(entry.getKey(),
                        StreamWrapper.newInstance(stream.inputStream,stream.name,stream.contentType,stream.autoClose)
                );
            }
        }
		return entity;
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
		keyValuePairs.put(key, value);
	}
	public void put(String key, File file) throws FileNotFoundException {
		put(key, file, null, null);
	}
	
	/**
	 * Adds a file to the request with both custom provided file content-type
	 * and file name
	 * 
	 * @param key
	 *            the key name for the new param.
	 * @param file
	 *            the file to add.
	 * @param contentType
	 *            the content type of the file, eg. application/json
	 * @throws FileNotFoundException
	 *             throws if wrong File argument was passed
	 */
	public void put(String key, File file, String contentType) throws FileNotFoundException {
		if (file == null || !file.exists()) {
			throw new FileNotFoundException();
		}
		if (key != null) {
			fileParams.put(key, new FileWrapper(file, contentType));
		}
	}

	public void put(String key, InputStream stream) {
		put(key, stream, null);
	}

	public void put(String key, InputStream stream, String name) {
		put(key, stream, name, null);
	}

	public void put(String key, InputStream stream, String name,
			String contentType) {
		put(key, stream, name, contentType, true);
	}

	/**
	 * Adds an input stream to the request.
	 * 
	 * @param key
	 *            the key name for the new param.
	 * @param stream
	 *            the input stream to add.
	 * @param name
	 *            the name of the stream.
	 * @param contentType
	 *            the content type of the file, eg. application/json
	 * @param autoClose
	 *            close input stream automatically on successful upload
	 */
	public void put(String key, InputStream stream, String name,
			String contentType, boolean autoClose) {
		if (key != null && stream != null) {
			streamParams.put(key, StreamWrapper.newInstance(stream, name,
					contentType, autoClose));
		}
	}

}
