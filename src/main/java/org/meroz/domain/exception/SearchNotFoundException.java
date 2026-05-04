package org.meroz.domain.exception;

public class SearchNotFoundException extends DomainException {
	public SearchNotFoundException(String message) {
		super("Search not found: %s".formatted(message));
	}
}
