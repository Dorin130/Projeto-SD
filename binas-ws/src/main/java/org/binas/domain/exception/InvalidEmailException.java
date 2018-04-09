package org.binas.domain.exception;

/** Exception used to signal a problem while initializing a station. */
public class InvalidEmailException extends Exception {
	private static final long serialVersionUID = 1L;

	public InvalidEmailException() {
	}

	public InvalidEmailException(String message) {
		super(message);
	}
}
