package com.uff.phenomanager.exception;

public class BadRequestApiException extends ApiException {

	private static final long serialVersionUID = 1L;

	public BadRequestApiException(String message) {
        super(message);
    }

    public BadRequestApiException(String message, Throwable throwable) {
        super(message, throwable);
    }

}