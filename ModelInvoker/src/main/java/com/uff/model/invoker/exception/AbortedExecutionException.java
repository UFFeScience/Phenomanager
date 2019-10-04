package com.uff.model.invoker.exception;

public class AbortedExecutionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AbortedExecutionException(String message) {
        super(message);
    }

    public AbortedExecutionException(String message, Throwable throwable) {
        super(message, throwable);
    }

}