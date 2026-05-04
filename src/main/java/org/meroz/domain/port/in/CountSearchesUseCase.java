package org.meroz.domain.port.in;

import org.meroz.domain.model.HotelSearch;

public interface CountSearchesUseCase {

	record CountResult(String searchId, HotelSearch search, long count) {}

	CountResult countBySearchId(String searchId);
}