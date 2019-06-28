package com.uff.phenomanager.exception;

public class InternalErrorApiException extends ApiException {

	private static final long serialVersionUID = 1L;

	public InternalErrorApiException(String message) {
        super(message);
    }

    public InternalErrorApiException(String message, Throwable throwable) {
        super(message, throwable);
    }

}