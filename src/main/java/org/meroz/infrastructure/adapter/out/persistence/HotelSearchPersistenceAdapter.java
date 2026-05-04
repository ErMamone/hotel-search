package org.meroz.infrastructure.adapter.out.persistence;

import org.meroz.domain.model.HotelSearch;
import org.meroz.domain.port.out.SearchRepositoryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class HotelSearchPersistenceAdapter implements SearchRepositoryPort {

	private final HotelSearchJpaRepository searchJpaRepository;

	public HotelSearchPersistenceAdapter(HotelSearchJpaRepository searchJpaRepository) {
		this.searchJpaRepository = searchJpaRepository;
	}

	@Override
	@Transactional
	public void save(HotelSearch hotelSearch) {
		searchJpaRepository.save(new HotelSearchEntity(hotelSearch.searchId(), hotelSearch.hotelId(),
				hotelSearch.checkIn(), hotelSearch.checkOut(), hotelSearch.ages()));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<HotelSearch> findFirstBySearchId(String searchId) {
		return searchJpaRepository.findFirstBySearchId(searchId)
				.map(e -> new HotelSearch(e.getSearchId(), e.getHotelId(), e.getCheckIn(),
						e.getCheckOut(), e.getAges()));
	}

	@Override
	@Transactional(readOnly = true)
	public long countBySearchId(String searchId) {
		return searchJpaRepository.countBySearchId(searchId);
	}
}
