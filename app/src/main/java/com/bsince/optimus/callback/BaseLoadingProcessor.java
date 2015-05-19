package com.bsince.optimus.callback;

public abstract class BaseLoadingProcessor implements LoadingProcessor {

	private final boolean shoudProcessOnUIThread ; 
	
	private int currentPercent = 0;
	
	private int totalPostTimes;
	

	public BaseLoadingProcessor(){
		this(true);
	}
	
	public BaseLoadingProcessor(boolean processOnUIThread){
		this.shoudProcessOnUIThread = processOnUIThread;
	}
	
	@Override
	public boolean isComplete() {
		return currentPercent==100;
	}

	@Override
	public boolean shouldProssingOnUIThread() {
		return shoudProcessOnUIThread;
	}

	
	public void configProcessTimes(int times){
		this.totalPostTimes = times;
	}
	
	@Override
	public int getCurrentPercent() {
		return currentPercent;
	}

	@Override
	public void setCurrentPercent(int percent) {
		this.currentPercent = percent;
	}

	public int getTotalPostTimes() {
		return totalPostTimes;
	}

}
