package org.meroz.infrastructure.adapter.in.rest.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SearchRequestDTOTest {

	@Test
	void shouldHandleNullAgesAsEmptyList() {
		var dto = new SearchRequestDTO("hotelA",
				LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5), null);

		assertThat(dto.ages()).isEmpty();
	}

	@Test
	void shouldCopyAgesToImmutableList() {
		var dto = new SearchRequestDTO("hotelA",
				LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5), List.of(30, 29));

		assertThat(dto.ages()).containsExactly(30, 29);
	}
}