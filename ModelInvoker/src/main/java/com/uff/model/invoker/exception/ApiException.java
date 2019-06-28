package com.uff.model.invoker.exception;

public abstract class ApiException extends Exception {

	private static final long serialVersionUID = 1L;

	public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable throwable) {
        super(message, throwable);
    }

}