package com.bsince.optimus.data;

import com.bsince.optimus.client.req.Method;
import com.bsince.optimus.client.respon.ResponseHandler;
import com.bsince.optimus.data.entity.SimpleMultipartEntity;
import com.bsince.optimus.event.Event;

import org.apache.http.HttpEntity;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MulitDataSet extends SimpleDataSet {

	protected final HashMap<String, StreamWrapper> streamParams = new HashMap<String, StreamWrapper>();
	protected final HashMap<String, FileWrapper> fileParams = new HashMap<String, FileWrapper>();
	private boolean isRepeatable;
	public MulitDataSet(String url) {
		this(Method.PUT, url);
	}

	public MulitDataSet(int method, String url) {

		super(method, url);
	}
	public void setRepeatable(boolean repeatable){
		this.isRepeatable = repeatable;
	}

	@Override
	protected void init() {
		setContentType(Constants.MULTIPART_CONTENT);
		setCharSet(Constants.DEFAULT_CHARSET);
	}
	
	@Override
	public String getBodyContentType() {
		//just set it int the entity
		return null;
	}

	@Override
	public HttpEntity getHttpEntity(Event<?> mEvent, ResponseHandler handler) {
		SimpleMultipartEntity entity = new SimpleMultipartEntity(mEvent, handler);
		entity.setIsRepeatable(isRepeatable);
		if(composite){
			 // Add object params
			List<BasicNameValuePair> pairs = convertParameters(null, keyValuePairs);
			for (BasicNameValuePair kv : pairs) {
	            entity.addPart(kv.getName(), kv.getValue(), Constants.CONTENT_TYPE_PLAIN);
	        }
		}else{
			 // Add string params
			for (Map.Entry<String, Object> entry : keyValuePairs.entrySet()) {
				entity.addPart(entry.getKey(), entry.getValue().toString(),Constants.CONTENT_TYPE_PLAIN);
			}
		}
		// Add stream params
        for (Map.Entry<String, StreamWrapper> entry : streamParams.entrySet()) {
            StreamWrapper stream = entry.getValue();
            if (stream.inputStream != null) {
                try {
					entity.addPart(entry.getKey(), stream.name, stream.inputStream,
					        stream.contentType);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					if(!mEvent.isCancel()){
						handler.responFail(mEvent, Constants.IO_EVENT_ERROR, e);
					}
				}
            }
        }
     // Add file params
        for (Map.Entry<String, FileWrapper> entry : fileParams.entrySet()) {
            FileWrapper fileWrapper = entry.getValue();
            entity.addPart(entry.getKey(), fileWrapper.file, fileWrapper.contentType);
        }
		
		return entity;
	}

	public void put(String key, File file) throws FileNotFoundException {
		put(key, file, null);
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

	public static class FileWrapper {
		public final File file;
		public final String contentType;
		public final String customFileName;

		public FileWrapper(File file, String contentType, String customFileName) {
			this.file = file;
			this.contentType = contentType;
			this.customFileName = customFileName;
		}
		public FileWrapper(File file, String contentType) {
			this.file = file;
			this.contentType = contentType;
			this.customFileName = file.getName();
		}
	}

	public static class StreamWrapper {
		public final InputStream inputStream;
		public final String name;
		public final String contentType;
		public final boolean autoClose;

		public StreamWrapper(InputStream inputStream, String name,
				String contentType, boolean autoClose) {
			this.inputStream = inputStream;
			this.name = name;
			this.contentType = contentType;
			this.autoClose = autoClose;
		}

		static StreamWrapper newInstance(InputStream inputStream, String name,
				String contentType, boolean autoClose) {
			return new StreamWrapper(inputStream, name,
					contentType == null ? Constants.APPLICATION_OCTET_STREAM
							: contentType, autoClose);
		}
	}

}
