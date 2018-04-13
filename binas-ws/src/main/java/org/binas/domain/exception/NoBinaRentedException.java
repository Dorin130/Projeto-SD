package org.binas.domain.exception;

/** Exception used to signal a problem while initializing a station. */
public class NoBinaRentedException extends Exception {
	private static final long serialVersionUID = 1L;

	public NoBinaRentedException() {
	}

	public NoBinaRentedException(String message) {
		super(message);
	}
}
