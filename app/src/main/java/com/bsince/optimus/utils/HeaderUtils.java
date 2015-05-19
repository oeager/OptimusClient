package com.bsince.optimus.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.protocol.HTTP;

import com.bsince.optimus.cache.HttpCache;

public class HeaderUtils {
	


    //解析响应头，最后封装成一个缓存entry
    public static HttpCache.Entry parseCacheHeaders(Map<String, String> headers,byte [] data) {
        long now = System.currentTimeMillis();
        long serverDate = 0;
        long lastModified = 0;
        long serverExpires = 0;
        long softExpire = 0;
        long maxAge = 0;
        boolean hasCacheControl = false;

        String serverEtag = null;
        String headerValue;
        
        headerValue = headers.get("Last-Modified");
        if(headerValue!=null){
        	lastModified = parseDateAsEpoch(headerValue);
        }

        headerValue = headers.get("Date");
        if (headerValue != null) {
            serverDate = parseDateAsEpoch(headerValue);
        }

        headerValue = headers.get("Cache-Control");
        if (headerValue != null) {
            hasCacheControl = true;
            String[] tokens = headerValue.split(",");
            for (int i = 0; i < tokens.length; i++) {
                String token = tokens[i].trim();
                if (token.equals("no-cache") || token.equals("no-store")) {
                    return null;
                } else if (token.startsWith("max-age=")) {
                    try {
                        maxAge = Long.parseLong(token.substring(8));
                    } catch (Exception e) {
                    }
                } else if (token.equals("must-revalidate") || token.equals("proxy-revalidate")) {
                    maxAge = 0;
                }
            }
        }

        headerValue = headers.get("Expires");
        if (headerValue != null) {
            serverExpires = parseDateAsEpoch(headerValue);
        }

        serverEtag = headers.get("ETag");

        // Cache-Control takes precedence over an Expires header, even if both exist and Expires
        // is more restrictive.
        if (hasCacheControl) {
            softExpire = now + maxAge * 1000;
        } else if (serverDate > 0 && serverExpires >= serverDate) {
            // Default semantic for Expire header in HTTP specification is softExpire.
            softExpire = now + (serverExpires - serverDate);
        }

        HttpCache.Entry entry = new HttpCache.Entry();
        entry.data = data;
        entry.etag = serverEtag;
        entry.softTtl = softExpire;
        entry.ttl = entry.softTtl;
        entry.serverDate = serverDate;
        entry.lastModifiedTime =lastModified ;
        entry.responseHeaders = headers;

        return entry;
    }

  
    /**
     * Parse date in RFC1123 format, and return its value as epoch
     */
    public static long parseDateAsEpoch(String dateStr) {
        try {
            // Parse date in RFC1123 format if this header contains one
            return DateUtils.parseDate(dateStr).getTime();
        } catch (DateParseException e) {
            // Date in invalid format, fallback to 0
            return 0;
        }
    }

    //从头中解析出编码格式，如果没有，便返回iso-8859-1;
    public static String parseCharset(Map<String, String> headers) {
        String contentType = headers.get(HTTP.CONTENT_TYPE);
        if (contentType != null) {
            String[] params = contentType.split(";");
            for (int i = 1; i < params.length; i++) {
                String[] pair = params[i].trim().split("=");
                if (pair.length == 2) {
                    if (pair[0].equals("charset")) {
                        return pair[1];
                    }
                }
            }
        }

        return HTTP.DEFAULT_CONTENT_CHARSET;
    }
  

	
	//----------------------
	 /**
     * 将header数组转化为map集合
     */
    public static Map<String, String> convertHeaders(Header[] headers) {
        Map<String, String> result = new HashMap<String, String>();
        for (int i = 0; i < headers.length; i++) {
            result.put(headers[i].getName(), headers[i].getValue());
        }
        return result;
    }
    
    public static void addCacheHeaders(HttpUriRequest request, HttpCache.Entry entry) {
        // 如果没有缓存条目，结束。
        if (entry == null) {
            return;
        }

        if (entry.etag != null) {
            request.setHeader("If-None-Match", entry.etag);
        }
        
        if(entry.lastModifiedTime>0){
        	 Date refTime = new Date(entry.lastModifiedTime);
        	 request.setHeader("If-Modified-Since", DateUtils.formatDate(refTime));
        	 return;
        }
        if (entry.serverDate > 0) {
            Date refTime = new Date(entry.serverDate);
            request.setHeader("If-Modified-Since", DateUtils.formatDate(refTime));
        }
    }
}
