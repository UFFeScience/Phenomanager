package com.uff.model.invoker.exception;

public class NotFoundApiException extends ApiException {

	private static final long serialVersionUID = 1L;

	public NotFoundApiException(String message) {
        super(message);
    }

    public NotFoundApiException(String message, Throwable throwable) {
        super(message, throwable);
    }

}