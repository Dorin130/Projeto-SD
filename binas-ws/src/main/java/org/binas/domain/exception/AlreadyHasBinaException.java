package org.binas.domain.exception;

/** Exception used to signal a problem while initializing a station. */
public class AlreadyHasBinaException extends Exception {
	private static final long serialVersionUID = 1L;

	public AlreadyHasBinaException() {
	}

	public AlreadyHasBinaException(String message) {
		super(message);
	}
}
