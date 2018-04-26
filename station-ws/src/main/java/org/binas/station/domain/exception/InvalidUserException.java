package org.binas.station.domain.exception;

/** Exception used to signal a problem while initializing a station. */
public class InvalidUserException extends Exception {
	private static final long serialVersionUID = 1L;

	public InvalidUserException() {
	}

	public InvalidUserException(String message) {
		super(message);
	}
}
