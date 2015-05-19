package com.bsince.optimus.cache;

import java.util.Collections;
import java.util.Map;

public interface HttpCache {

	public Entry get(String key);

	public void put(String key, Entry entry);

	public void init();

	public void invalidate(String key, boolean fullExpire);

	public void remove(String key);

	public void clear();
	
	

	//尝试着以inputstram作为data，但不幸的是，因为我们既要将其转为T这个泛对象，又要将其写入file中，inputstream不支持clone，所以以byte[]作为中转，但如此大数据下载将是不被允许的
	public static class Entry {

		public byte [] data;
		
		public String etag;

		public long lastModifiedTime;

		public long serverDate;

		public long ttl;

		public long softTtl;

		public Map<String, String> responseHeaders = Collections.emptyMap();

		public boolean isExpired() {
			return this.ttl < System.currentTimeMillis();
		}

		public boolean refreshNeeded() {
			return this.softTtl < System.currentTimeMillis();
		}
	}
}
