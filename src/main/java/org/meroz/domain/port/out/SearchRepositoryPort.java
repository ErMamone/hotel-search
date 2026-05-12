package org.meroz.domain.port.out;

import org.meroz.domain.model.HotelSearch;

import java.util.Optional;

public interface SearchRepositoryPort {

	void save(HotelSearch hotelSearch);

	Optional<HotelSearch> findFirstBySearchId(String searchId);

	long countBySearchId(String searchId);
}
