package org.meroz.application.service;

import org.meroz.application.port.in.CountSearchesUseCase;
import org.meroz.domain.exception.SearchNotFoundException;
import org.meroz.domain.model.HotelSearch;
import org.meroz.domain.port.out.SearchRepositoryPort;

public class CountSearchesService implements CountSearchesUseCase {

	private final SearchRepositoryPort repository;

	public CountSearchesService(SearchRepositoryPort repository) {
		this.repository = repository;
	}

	@Override
	public CountResult countBySearchId(String searchId) {
		HotelSearch search = repository.findFirstBySearchId(searchId)
				.orElseThrow(() -> new SearchNotFoundException(searchId));

		long count = repository.countBySearchId(searchId);

		return new CountResult(searchId, search, count);
	}
}