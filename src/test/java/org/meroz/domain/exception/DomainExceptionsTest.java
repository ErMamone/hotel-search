package org.meroz.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainExceptionsTest {

	@Test
	void invalidDateRangeException_shouldHoldMessage() {
		var ex = new InvalidDateRangeException("bad range");
		assertThat(ex.getMessage()).isEqualTo("bad range");
		assertThat(ex).isInstanceOf(DomainException.class);
	}

	@Test
	void searchNotFoundException_shouldFormatSearchId() {
		var ex = new SearchNotFoundException("abc123");
		assertThat(ex.getMessage()).isEqualTo("Search not found: abc123");
	}

	@Test
	void eventPublishException_shouldPreserveCause() {
		var cause = new RuntimeException("kafka down");
		var ex = new EventPublishException("publish failed", cause);
		assertThat(ex.getMessage()).isEqualTo("publish failed");
		assertThat(ex.getCause()).isSameAs(cause);
	}
}