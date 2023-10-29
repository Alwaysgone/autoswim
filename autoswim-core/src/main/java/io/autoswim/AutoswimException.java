package io.autoswim;

public class AutoswimException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public AutoswimException(String message, Throwable t) {
		super(message, t);
	}
	
	public AutoswimException(String message) {
		super(message);
	}
}
