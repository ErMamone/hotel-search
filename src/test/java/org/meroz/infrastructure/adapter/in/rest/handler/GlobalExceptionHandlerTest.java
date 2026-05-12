package org.meroz.infrastructure.adapter.in.rest.handler;

import org.junit.jupiter.api.Test;
import org.meroz.domain.exception.EventPublishException;
import org.meroz.domain.exception.InvalidDateRangeException;
import org.meroz.domain.exception.SearchNotFoundException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

	public static final String ERROR_KEY = "error";
	public static final String ERRORS_KEY = "errors";

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	@Test
	void shouldFallbackToInvalidWhenFieldErrorMessageIsNull() throws Exception {
		var target = new Object();
		var bindingResult = new BeanPropertyBindingResult(target, "target");
		bindingResult.addError(new FieldError("target", "fieldX", null, false, null, null, null));

		Method dummyMethod = String.class.getMethod("toString");
		var methodParameter = new org.springframework.core.MethodParameter(dummyMethod, -1);
		var ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

		var response = handler.handleValidation(ex);

		assertThat(response.getStatusCode().value()).isEqualTo(400);

		assertNotNull(response.getBody());
		@SuppressWarnings("unchecked")
		var errors = (java.util.Map<String, Object>) response.getBody().get(ERRORS_KEY);
		assertThat(errors).containsEntry("fieldX", "invalid");
	}

	@Test
	void shouldUseProvidedMessageWhenFieldErrorMessageExists() throws Exception {
		var target = new Object();
		var bindingResult = new BeanPropertyBindingResult(target, "target");
		bindingResult.addError(new FieldError("target", "fieldY", null, false, null, null, "must not be blank"));

		Method dummyMethod = String.class.getMethod("toString");
		var methodParameter = new org.springframework.core.MethodParameter(dummyMethod, -1);
		var ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

		var response = handler.handleValidation(ex);

		assertNotNull(response.getBody());
		@SuppressWarnings("unchecked")
		var errors = (java.util.Map<String, Object>) response.getBody().get(ERRORS_KEY);
		assertThat(errors).containsEntry("fieldY", "must not be blank");
	}

	@Test
	void shouldReturn400ForInvalidDateRange() {
		var response = handler.handleDateRange(new InvalidDateRangeException("bad range"));
		assertAll(
				() -> assertThat(response.getStatusCode().value()).isEqualTo(400),
				() -> assertThat(response.getBody()).containsEntry(ERROR_KEY, "bad range")
		);
	}

	@Test
	void shouldReturn404ForSearchNotFound() {
		var response = handler.handleNotFound(new SearchNotFoundException("xyz"));
		assertAll(
				() -> assertThat(response.getStatusCode().value()).isEqualTo(404),
				() -> {
					assertNotNull(response.getBody());
					assertThat(response.getBody().get(ERROR_KEY)).asString().contains("xyz");
				}
		);
	}

	@Test
	void shouldReturn503ForEventPublish() {
		var response = handler.handleEventPublish(
				new EventPublishException("boom", new RuntimeException()));
		assertThat(response.getStatusCode().value()).isEqualTo(503);
	}

	@Test
	void shouldReturn500ForUnexpected() {
		var response = handler.handleUnexpected(new RuntimeException("x"));
		assertAll(
				() -> assertThat(response.getStatusCode().value()).isEqualTo(500),
				() -> assertThat(response.getBody()).containsEntry(ERROR_KEY, "Unexpected error")
		);
	}

	@Test
	void shouldReturn400ForUnreadableBodyWithDateCause() {
		var cause = mock(com.fasterxml.jackson.databind.exc.InvalidFormatException.class);
		when(cause.getPath()).thenReturn(List.of(
				new com.fasterxml.jackson.databind.JsonMappingException.Reference(null, "checkIn")));
		var ex = new HttpMessageNotReadableException("x", cause, null);

		var response = handler.handleNotReadable(ex);

		assertThat(response.getStatusCode().value()).isEqualTo(400);
		assertNotNull(response.getBody());
		assertThat(response.getBody().get(ERROR_KEY)).asString().contains("checkIn");
	}

	@Test
	void shouldReturn400ForUnreadableBodyWithoutCause() {
		var ex = new HttpMessageNotReadableException("x", null, null);

		var response = handler.handleNotReadable(ex);

		assertThat(response.getBody()).containsEntry(ERROR_KEY, "Invalid request body");
	}

	@Test
	void shouldReturn400ForUnreadableBodyWithEmptyPath() {
		var cause = mock(com.fasterxml.jackson.databind.exc.InvalidFormatException.class);
		when(cause.getPath()).thenReturn(List.of());
		var ex = new HttpMessageNotReadableException("x", cause, null);

		var response = handler.handleNotReadable(ex);

		assertAll(
				() -> assertThat(response.getStatusCode().value()).isEqualTo(400),
				() -> assertNotNull(response.getBody()),
				() -> {
					assertNotNull(response.getBody());
					assertThat(response.getBody().get(ERROR_KEY)).asString().contains("'field'");
				}
		);
	}

	@Test
	void shouldReturn400ForConstraintViolation() {
		var ex = new jakarta.validation.ConstraintViolationException(
				"age must be zero or positive", java.util.Set.of());

		var response = handler.handleConstraintViolation(ex);

		assertAll(
				() -> assertThat(response.getStatusCode().value()).isEqualTo(400),
				() -> assertThat(response.getBody()).containsEntry(ERROR_KEY, "age must be zero or positive")
		);
	}
}