package com.rockburger.burgermain.domain.exception;

public class NameAlreadyExistsException extends RuntimeException{
	public NameAlreadyExistsException(String message) {
		super(message);
	}
}
