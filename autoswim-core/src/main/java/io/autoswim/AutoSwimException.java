package io.autoswim;

public class AutoSwimException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public AutoSwimException(String message, Throwable t) {
		super(message, t);
	}
	
	public AutoSwimException(String message) {
		super(message);
	}
}
