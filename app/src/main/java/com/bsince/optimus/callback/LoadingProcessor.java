package com.bsince.optimus.callback;

public interface LoadingProcessor {

	void onProcessing(int percent, long currentSize, long totalSize);

	boolean isComplete();

	boolean shouldProssingOnUIThread();

	int getCurrentPercent();

	void setCurrentPercent(int percent);

	/**
	 * 配置刷新频率，这里以总刷新次数为设置值。 如：设置为100，则进度每更新1%刷新一次UI
	 * 
	 * @param times
	 */
	void configProcessTimes(int times);
	
	int getTotalPostTimes();

}
