package com.bsince.optimus.client;

import com.bsince.optimus.event.Event;

public class NetworkTask<T> implements Runnable {

	private final Event<T> mEvent;

	private final NetExecutor mExecutor;


	public NetworkTask(NetExecutor mExecutor,Event<T> event) {
		this.mEvent = event;
		this.mExecutor = mExecutor;
	}

	@Override
	public void run() {
		mExecutor.execute(mEvent);
	}


}
