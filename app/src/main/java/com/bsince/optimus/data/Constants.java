package com.bsince.optimus.data;

import org.apache.http.protocol.HTTP;

public class Constants {
	
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	
    public static final String HEADER_CONTENT_RANGE = "Content-Range";
    
    public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
    
    public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    
    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    
    public static final String ENCODING_GZIP = "gzip";
    
    public static final int DEFAULT_DISK_USAGE_BYTES = 5 * 1024 * 1024;
    
    public static final int DEFAULT_MAX_CONNECTIONS = 10;
    
    public static final int DEFAULT_SOCKET_TIMEOUT = 10 * 1000;
    
    public static final int DEFAULT_MAX_RETRIES = 5;
    
    public static final int DEFAULT_RETRY_SLEEP_TIME_MILLIS = 1500;
    
    public static final int DEFAULT_SOCKET_BUFFER_SIZE = 8192;
    
    public static final int BUFFER_SIZE = 4096;
    
    public final static String DEFAULT_CHARSET = HTTP.UTF_8;
    
	public final static String DEFAULT_CONTENT = "application/x-www-form-urlencoded";
	
	public final static String JSON_CONTENT = "application/json";
	
	public final static String MULTIPART_CONTENT = "multipart/form-data";

    public static final String CONTENT_TYPE_PLAIN="text/plain";
	
	public final static String APPLICATION_OCTET_STREAM =
            "application/octet-stream";
	
	public static final int IO_EVENT_ERROR = -1;
	
	public static int DEFAULT_POOL_SIZE = 4096;
	
}
