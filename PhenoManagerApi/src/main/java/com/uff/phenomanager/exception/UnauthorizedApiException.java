package com.uff.phenomanager.exception;

public class UnauthorizedApiException extends ApiException {

	private static final long serialVersionUID = 1L;

	public UnauthorizedApiException(String message) {
        super(message);
    }

    public UnauthorizedApiException(String message, Throwable throwable) {
        super(message, throwable);
    }

}