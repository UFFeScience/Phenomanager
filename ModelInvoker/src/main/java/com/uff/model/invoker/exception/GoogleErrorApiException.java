package com.uff.model.invoker.exception;

public class GoogleErrorApiException extends ApiException {

	private static final long serialVersionUID = 1L;

	public GoogleErrorApiException(String message) {
        super(message);
    }

    public GoogleErrorApiException(String message, Throwable throwable) {
        super(message, throwable);
    }

}