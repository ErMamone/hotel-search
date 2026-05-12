package org.meroz.infrastructure.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.meroz.infrastructure.adapter.in.rest.mapper.HotelSearchMapper;
import org.meroz.domain.exception.EventPublishException;
import org.meroz.domain.exception.InvalidDateRangeException;
import org.meroz.domain.exception.SearchNotFoundException;
import org.meroz.domain.model.HotelSearch;
import org.meroz.application.port.in.CountSearchesUseCase;
import org.meroz.application.port.in.CountSearchesUseCase.CountResult;
import org.meroz.application.port.in.CreateSearchUseCase;
import org.meroz.infrastructure.adapter.in.rest.handler.GlobalExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

	private static final LocalDate CHECK_IN = LocalDate.now().plusMonths(1);

	private static final LocalDate CHECK_OUT = CHECK_IN.plusDays(4);

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
				LocalDate.of(2025, 1, 1),
				LocalDate.of(2025, 1, 5), List.of(30, 29));
		when(createSearchUseCase.createSearch(any())).thenReturn(search);

		var body = Map.of(
				"hotelId", "hotelA",
				"checkIn", CHECK_IN.format(DATE_FORMAT),
				"checkOut", CHECK_OUT.format(DATE_FORMAT),
				"ages", List.of(30, 29)
		);

		mockMvc.perform(post("/search")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.searchId").value("abc123"));
	}

	@Test
	void shouldReturn400OnValidationError() throws Exception {
		var body = Map.of(
				"hotelId", "",
				"checkIn", CHECK_IN.format(DATE_FORMAT),
				"checkOut", CHECK_OUT.format(DATE_FORMAT),
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
				"checkIn", CHECK_IN.format(DATE_FORMAT),
				"checkOut", CHECK_OUT.format(DATE_FORMAT),
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
				"checkIn", CHECK_IN.format(DATE_FORMAT),
				"checkOut", CHECK_OUT.format(DATE_FORMAT),
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
				"checkIn", CHECK_IN.format(DATE_FORMAT),
				"checkOut", CHECK_OUT.format(DATE_FORMAT),
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
				"checkIn", CHECK_IN.format(DATE_FORMAT),
				"checkOut", CHECK_OUT.format(DATE_FORMAT),
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

	@Test
	void shouldReturn400WhenDateFormatIsInvalid() throws Exception {
		var body = """
			{
			  "hotelId": "hotelA",
			  "checkIn": "2026-05-19",
			  "checkOut": "2026-05-26",
			  "ages": [30, 29]
			}
			""";

		mockMvc.perform(post("/search")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").exists());
	}

	@Test
	void shouldReturn400WhenBodyIsMalformedJson() throws Exception {
		mockMvc.perform(post("/search")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{ not json"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("Invalid request body"));
	}

	@Test
	void shouldReturn400WhenCheckInIsPast() throws Exception {
		var body = Map.of(
				"hotelId", "hotelA",
				"checkIn", "01/01/2020",
				"checkOut", "05/01/2020",
				"ages", List.of(30)
		);

		mockMvc.perform(post("/search")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.checkIn").exists());
	}

	@Test
	void shouldReturn400WhenAgeIsNegative() throws Exception {
		var body = Map.of(
				"hotelId", "hotelA",
				"checkIn", CHECK_IN.format(DATE_FORMAT),
				"checkOut", CHECK_OUT.format(DATE_FORMAT),
				"ages", List.of(-1, 30)
		);

		mockMvc.perform(post("/search")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isBadRequest());
	}
}