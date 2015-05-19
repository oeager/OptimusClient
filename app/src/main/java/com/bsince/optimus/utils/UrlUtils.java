package com.bsince.optimus.utils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;

import com.bsince.optimus.data.DataSet;

public class UrlUtils {


	public static String getUrlWithQueryString(boolean shouldEncodeUrl,
			String url, DataSet params) {
		if (url == null)
			return null;

		if (shouldEncodeUrl) {
			try {
				String decodedURL = URLDecoder.decode(url, "UTF-8");
				URL _url = new URL(decodedURL);
				URI _uri = new URI(_url.getProtocol(), _url.getUserInfo(),
						_url.getHost(), _url.getPort(), _url.getPath(),
						_url.getQuery(), _url.getRef());
				url = _uri.toASCIIString();
			} catch (Exception ex) {
				// Should not really happen, added just for sake of validity
				L.e( "getUrlWithQueryString encoding URL", ex);
			}
		}

		if (params != null) {
			// Construct the query string and trim it, in case it
			// includes any excessive white spaces.
			String paramString;
			try {
				paramString = params.getDatasetString().trim();
				if (!paramString.equals("") && !paramString.equals("?")) {
					url += url.contains("?") ? "&" : "?";
					url += paramString;
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			// Only add the query string if it isn't empty and it
			// isn't equal to '?'.

		}

		return url;
	}
}
