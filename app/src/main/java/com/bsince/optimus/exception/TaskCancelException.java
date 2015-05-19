package com.bsince.optimus.exception;

public class TaskCancelException extends Exception {

	private static final long serialVersionUID = 1L;

	public TaskCancelException() {
		super();
	}

	public TaskCancelException(String detailMessage) {
		super(detailMessage);
	}

	public TaskCancelException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public TaskCancelException(Throwable throwable) {
		super(throwable);
	}
}
