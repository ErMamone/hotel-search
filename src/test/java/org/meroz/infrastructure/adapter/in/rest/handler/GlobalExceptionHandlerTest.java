package org.meroz.infrastructure.adapter.in.rest.handler;

import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

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
		@SuppressWarnings("unchecked")
		var errors = (java.util.Map<String, Object>) response.getBody().get("errors");
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

		@SuppressWarnings("unchecked")
		var errors = (java.util.Map<String, Object>) response.getBody().get("errors");
		assertThat(errors).containsEntry("fieldY", "must not be blank");
	}
}