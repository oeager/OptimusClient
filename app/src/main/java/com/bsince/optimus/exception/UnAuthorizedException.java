package com.bsince.optimus.exception;

import java.io.IOException;

public class UnAuthorizedException extends IOException {

	private static final long serialVersionUID = 1L;

	public UnAuthorizedException() {
		super();
	}

	public UnAuthorizedException(String detailMessage) {
		super(detailMessage);
	}

	public UnAuthorizedException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public UnAuthorizedException(Throwable throwable) {
		super(throwable);
	}

}
