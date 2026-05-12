package org.meroz.infrastructure.adapter.in.rest.handler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.meroz.domain.exception.EventPublishException;
import org.meroz.domain.exception.InvalidDateRangeException;
import org.meroz.domain.exception.SearchNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	private static final String ERROR_KEY = "error";

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException exception) {
		Map<String, Object> errors = exception.getBindingResult().getFieldErrors().stream()
				.collect(Collectors.toMap(
						FieldError::getField,
						f -> f.getDefaultMessage() == null ? "invalid" : f.getDefaultMessage(),
						(a, b) -> a));

		return ResponseEntity.badRequest().body(Map.of("errors", errors));
	}

	@ExceptionHandler(InvalidDateRangeException.class)
	public ResponseEntity<Map<String, String>> handleDateRange(InvalidDateRangeException ex) {
		return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, ex.getMessage()));
	}

	@ExceptionHandler(SearchNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleNotFound(SearchNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_KEY, ex.getMessage()));
	}

	@ExceptionHandler(EventPublishException.class)
	public ResponseEntity<Map<String, String>> handleEventPublish(EventPublishException ex) {
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body(Map.of(ERROR_KEY, "Event publishing failed, try again later"));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, String>> handleNotReadable(HttpMessageNotReadableException ex) {
		String message = "Invalid request body";
		if (ex.getCause() instanceof InvalidFormatException ife) {
			String field = ife.getPath().isEmpty() ? "field" : ife.getPath().getFirst().getFieldName();
			message = "Invalid format for field '%s', expected dd/MM/yyyy".formatted(field);
		}
		return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, message));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
		return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, ex.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> handleUnexpected(Exception ex) {
		log.error("Unexpected error", ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of(ERROR_KEY, "Unexpected error"));
	}
}
