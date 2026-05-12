package org.meroz.infrastructure.adapter.in.rest.mapper;

import org.junit.jupiter.api.Test;
import org.meroz.application.port.in.CountSearchesUseCase.CountResult;
import org.meroz.domain.model.HotelSearch;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class HotelSearchMapperTest {

	private final HotelSearchMapper mapper = new HotelSearchMapper();

	@Test
	void shouldMapCountResultToCountResponseDTO() {
		var search = new HotelSearch("id1", "hotelA",
				LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5), List.of(30, 29));
		var result = new CountResult("id1", search, 7L);

		var dto = mapper.toCountResponse(result);

		assertAll(
				() -> assertThat(dto.searchId()).isEqualTo("id1"),
				() -> assertThat(dto.count()).isEqualTo(7L),
				() -> assertThat(dto.search().hotelId()).isEqualTo("hotelA"),
				() -> assertThat(dto.search().checkIn()).isEqualTo(LocalDate.of(2025, 1, 1)),
				() -> assertThat(dto.search().checkOut()).isEqualTo(LocalDate.of(2025, 1, 5)),
				() -> assertThat(dto.search().ages()).containsExactly(30, 29)
		);
	}
}
