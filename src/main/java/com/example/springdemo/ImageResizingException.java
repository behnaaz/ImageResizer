package com.example.springdemo;

/**
 * Custom Exception class to wrap an exception 
 * @author B 
 *
 */
public class ImageResizingException extends Exception {
	private static final long serialVersionUID = 1L;
	private String message;
	private Exception cause;

	public ImageResizingException(String message) {
		this.message = message;
	}

	public ImageResizingException(Exception cause) {
		this.message = cause.getMessage();
		this.cause = cause;
	}

	public String getMessage() {
		return message;
	}
	
	public Exception getCause() {
		return cause;
	}
}
