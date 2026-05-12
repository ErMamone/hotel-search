package org.meroz.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.meroz.domain.exception.SearchNotFoundException;
import org.meroz.domain.model.HotelSearch;
import org.meroz.domain.port.out.SearchRepositoryPort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CountSearchesServiceTest {

	private SearchRepositoryPort repository;
	private CountSearchesService service;

	@BeforeEach
	void setUp() {
		repository = mock(SearchRepositoryPort.class);
		service = new CountSearchesService(repository);
	}

	@Test
	void shouldReturnCountResultWhenSearchExists() {
		var search = new HotelSearch("id1", "hotelA",
				LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5), List.of(30));
		when(repository.findFirstBySearchId("id1")).thenReturn(Optional.of(search));
		when(repository.countBySearchId("id1")).thenReturn(5L);

		var result = service.countBySearchId("id1");

		assertAll(
				() -> assertThat(result.searchId()).isEqualTo("id1"),
				() -> assertThat(result.search()).isEqualTo(search),
				() -> assertThat(result.count()).isEqualTo(5L)
		);
	}

	@Test
	void shouldThrowWhenSearchNotFound() {
		when(repository.findFirstBySearchId("missing")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.countBySearchId("missing"))
				.isInstanceOf(SearchNotFoundException.class)
				.hasMessageContaining("missing");

		verify(repository, never()).countBySearchId(any());
	}
}
