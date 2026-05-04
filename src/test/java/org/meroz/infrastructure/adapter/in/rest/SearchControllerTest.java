package org.meroz.infrastructure.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.meroz.application.mapper.HotelSearchMapper;
import org.meroz.domain.exception.EventPublishException;
import org.meroz.domain.exception.InvalidDateRangeException;
import org.meroz.domain.exception.SearchNotFoundException;
import org.meroz.domain.model.HotelSearch;
import org.meroz.domain.port.in.CountSearchesUseCase;
import org.meroz.domain.port.in.CountSearchesUseCase.CountResult;
import org.meroz.domain.port.in.CreateSearchUseCase;
import org.meroz.infrastructure.adapter.in.rest.handler.GlobalExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SearchController.class)
@Import({GlobalExceptionHandler.class, HotelSearchMapper.class})
class SearchControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private CreateSearchUseCase createSearchUseCase;

	@MockitoBean
	private CountSearchesUseCase countSearchesUseCase;

	@Test
	void shouldReturnSearchIdOnValidRequest() throws Exception {
		var search = new HotelSearch("abc123", "hotelA",
				LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5), List.of(30, 29));
		when(createSearchUseCase.createSearch(any())).thenReturn(search);

		var body = Map.of(
				"hotelId", "hotelA",
				"checkIn", "01/01/2025",
				"checkOut", "05/01/2025",
				"ages", List.of(30, 29)
		);

		mockMvc.perform(post("/search")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.searchId").value("abc123"));
	}

	@Test
	void shouldReturn400OnValidationError() throws Exception {
		var body = Map.of(
				"hotelId", "",
				"checkIn", "01/01/2025",
				"checkOut", "05/01/2025",
				"ages", List.of(30)
		);

		mockMvc.perform(post("/search")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors").exists());
	}

	@Test
	void shouldReturn400WhenHotelIdNotAlphanumeric() throws Exception {
		var body = Map.of(
				"hotelId", "hotel-A!",
				"checkIn", "01/01/2025",
				"checkOut", "05/01/2025",
				"ages", List.of(30)
		);

		mockMvc.perform(post("/search")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturn400WhenAgesEmpty() throws Exception {
		var body = Map.of(
				"hotelId", "hotelA",
				"checkIn", "01/01/2025",
				"checkOut", "05/01/2025",
				"ages", List.of()
		);

		mockMvc.perform(post("/search")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturn400OnInvalidDateRange() throws Exception {
		when(createSearchUseCase.createSearch(any()))
				.thenThrow(new InvalidDateRangeException("checkIn must be before checkOut"));

		var body = Map.of(
				"hotelId", "hotelA",
				"checkIn", "05/01/2025",
				"checkOut", "01/01/2025",
				"ages", List.of(30)
		);

		mockMvc.perform(post("/search")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("checkIn must be before checkOut"));
	}

	@Test
	void shouldReturnCountOnExistingSearch() throws Exception {
		var search = new HotelSearch("id1", "hotelA",
				LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5), List.of(30));
		when(countSearchesUseCase.countBySearchId("id1"))
				.thenReturn(new CountResult("id1", search, 3L));

		mockMvc.perform(get("/count").param("searchId", "id1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.searchId").value("id1"))
				.andExpect(jsonPath("$.count").value(3))
				.andExpect(jsonPath("$.search.hotelId").value("hotelA"))
				.andExpect(jsonPath("$.search.checkIn").value("01/01/2025"));
	}

	@Test
	void shouldReturn404WhenSearchNotFound() throws Exception {
		when(countSearchesUseCase.countBySearchId("missing"))
				.thenThrow(new SearchNotFoundException("missing"));

		mockMvc.perform(get("/count").param("searchId", "missing"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("Search not found: missing"));
	}

	@Test
	void shouldReturn503OnEventPublishFailure() throws Exception {
		when(createSearchUseCase.createSearch(any()))
				.thenThrow(new EventPublishException("publish failed", new RuntimeException("kafka down")));

		var body = Map.of(
				"hotelId", "hotelA",
				"checkIn", "01/01/2025",
				"checkOut", "05/01/2025",
				"ages", List.of(30)
		);

		mockMvc.perform(post("/search")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isServiceUnavailable());
	}

	@Test
	void shouldReturn500OnUnexpectedError() throws Exception {
		when(countSearchesUseCase.countBySearchId(any()))
				.thenThrow(new RuntimeException("boom"));

		mockMvc.perform(get("/count").param("searchId", "id1"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.error").value("Unexpected error"));
	}
}