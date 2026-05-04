package org.meroz.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.meroz.domain.exception.InvalidDateRangeException;
import org.meroz.domain.model.HotelSearch;
import org.meroz.domain.port.in.CreateSearchCommand;
import org.meroz.domain.port.out.SearchEventPublisherPort;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateSearchServiceTest {

	private SearchEventPublisherPort publisher;
	private CreateSearchService service;

	@BeforeEach
	void setUp() {
		publisher = mock(SearchEventPublisherPort.class);
		service = new CreateSearchService(publisher);
	}

	@Test
	void shouldGenerateSearchIdAndPublish() {
		var command = new CreateSearchCommand("hotelA",
				LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5), List.of(30, 29, 1));

		HotelSearch result = service.createSearch(command);

		assertThat(result.searchId()).isNotBlank().hasSize(32);
		assertThat(result.hotelId()).isEqualTo("hotelA");
		verify(publisher).publish(any(HotelSearch.class));
	}

	@Test
	void shouldGenerateSameIdForSameInput() {
		var command1 = new CreateSearchCommand("hotelA",
				LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5), List.of(30, 29, 1));
		var command2 = new CreateSearchCommand("hotelA",
				LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5), List.of(30, 29, 1));

		var r1 = service.createSearch(command1);
		var r2 = service.createSearch(command2);

		assertThat(r1.searchId()).isEqualTo(r2.searchId());
	}

	@Test
	void shouldGenerateDifferentIdWhenAgesOrderDiffers() {
		var c1 = new CreateSearchCommand("hotelA",
				LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5), List.of(1, 2, 3));
		var c2 = new CreateSearchCommand("hotelA",
				LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5), List.of(3, 2, 1));

		assertThat(service.createSearch(c1).searchId())
				.isNotEqualTo(service.createSearch(c2).searchId());
	}

	@Test
	void shouldGenerateDifferentIdForDifferentHotel() {
		var c1 = new CreateSearchCommand("hotelA",
				LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5), List.of(30));
		var c2 = new CreateSearchCommand("hotelB",
				LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5), List.of(30));

		assertThat(service.createSearch(c1).searchId())
				.isNotEqualTo(service.createSearch(c2).searchId());
	}

	@Test
	void shouldThrowWhenCheckInEqualsCheckOut() {
		var date = LocalDate.of(2025, 1, 1);
		var command = new CreateSearchCommand("hotelA", date, date, List.of(30));

		assertThatThrownBy(() -> service.createSearch(command))
				.isInstanceOf(InvalidDateRangeException.class)
				.hasMessageContaining("checkIn must be before checkOut");

		verifyNoInteractions(publisher);
	}

	@Test
	void shouldThrowWhenCheckInAfterCheckOut() {
		var command = new CreateSearchCommand("hotelA",
				LocalDate.of(2025, 1, 5), LocalDate.of(2025, 1, 1), List.of(30));

		assertThatThrownBy(() -> service.createSearch(command))
				.isInstanceOf(InvalidDateRangeException.class);

		verifyNoInteractions(publisher);
	}
}