package org.meroz.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.meroz.domain.model.HotelSearch;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HotelSearchPersistenceAdapterUnitTest {

	private HotelSearchJpaRepository jpaRepository;
	private HotelSearchPersistenceAdapter adapter;

	@BeforeEach
	void setUp() {
		jpaRepository = mock(HotelSearchJpaRepository.class);
		adapter = new HotelSearchPersistenceAdapter(jpaRepository);
	}

	@Test
	void shouldMapDomainToEntityAndSave() {
		var search = new HotelSearch("id1", "hotelA",
				LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5), List.of(30, 29));

		adapter.save(search);

		verify(jpaRepository).save(any(HotelSearchEntity.class));
	}

	@Test
	void shouldMapEntityToDomainOnFind() {
		var entity = new HotelSearchEntity("id1", "hotelA",
				LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5), List.of(30, 29));
		when(jpaRepository.findFirstBySearchId("id1")).thenReturn(Optional.of(entity));

		var result = adapter.findFirstBySearchId("id1");

		assertThat(result).isPresent();
		assertThat(result.get().searchId()).isEqualTo("id1");
		assertThat(result.get().hotelId()).isEqualTo("hotelA");
		assertThat(result.get().checkIn()).isEqualTo(LocalDate.of(2025, 1, 1));
		assertThat(result.get().checkOut()).isEqualTo(LocalDate.of(2025, 1, 5));
		assertThat(result.get().ages()).containsExactly(30, 29);
	}

	@Test
	void shouldReturnEmptyWhenNotFound() {
		when(jpaRepository.findFirstBySearchId("missing")).thenReturn(Optional.empty());

		assertThat(adapter.findFirstBySearchId("missing")).isEmpty();
	}

	@Test
	void shouldDelegateCount() {
		when(jpaRepository.countBySearchId("id1")).thenReturn(5L);

		assertThat(adapter.countBySearchId("id1")).isEqualTo(5L);
	}
}