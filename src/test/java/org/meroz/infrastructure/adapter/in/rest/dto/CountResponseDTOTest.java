package org.meroz.infrastructure.adapter.in.rest.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class CountResponseDTOTest {

	@Test
	void searchPayloadShouldCopyAges() {
		var ages = new ArrayList<>(List.of(30, 29));
		var payload = new CountResponseDTO.SearchPayload("hotelA",
				LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5), ages);

		ages.add(99);

		assertThat(payload.ages()).containsExactly(30, 29);
	}

	@Test
	void payloadAgesShouldBeImmutable() {
		var payload = new CountResponseDTO.SearchPayload("hotelA",
				LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5), List.of(30));
		var ages = payload.ages();

		assertThatThrownBy(() -> ages.add(99))
				.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void shouldExposeAllFields() {
		var payload = new CountResponseDTO.SearchPayload("hotelA",
				LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5), List.of(30));
		var dto = new CountResponseDTO("id1", payload, 5L);

		assertAll(
				() -> assertThat(dto.searchId()).isEqualTo("id1"),
				() -> assertThat(dto.search()).isEqualTo(payload),
				() -> assertThat(dto.count()).isEqualTo(5L)
		);
	}
}
