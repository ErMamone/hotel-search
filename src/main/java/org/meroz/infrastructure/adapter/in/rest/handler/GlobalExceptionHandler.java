package org.meroz.infrastructure.adapter.in.rest.handler;

import lombok.extern.slf4j.Slf4j;
import org.meroz.domain.exception.EventPublishException;
import org.meroz.domain.exception.InvalidDateRangeException;
import org.meroz.domain.exception.SearchNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException exception){
		var errors = exception.getBindingResult().getFieldErrors().stream()
				.collect(Collectors.toMap(
						FieldError::getField,
						f -> f.getDefaultMessage() == null ? "invalid" : f.getDefaultMessage(),
						(a,b) -> a));

		return ResponseEntity.badRequest().body(Map.of("errors", errors));
	}

	@ExceptionHandler(InvalidDateRangeException.class)
	public ResponseEntity<Map<String, String>> handleDateRange(InvalidDateRangeException ex) {
		return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(SearchNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleNotFound(SearchNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(EventPublishException.class)
	public ResponseEntity<Map<String, String>> handleEventPublish(EventPublishException ex) {
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body(Map.of("error", "Event publishing failed, try again later"));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> handleUnexpected(Exception ex) {
			log.error("Unexpected error", ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of("error", "Unexpected error"));
	}
}
