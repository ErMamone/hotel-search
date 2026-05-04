package org.meroz.application.mapper;

import org.meroz.domain.port.in.CountSearchesUseCase;
import org.meroz.infrastructure.adapter.in.rest.dto.CountResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class HotelSearchMapper {

	public CountResponseDTO toCountResponse(CountSearchesUseCase.CountResult result) {
		var search = result.search();
		return new CountResponseDTO(
				result.searchId(),
				new CountResponseDTO.SearchPayload(
						search.hotelId(), search.checkIn(), search.checkOut(), search.ages()
				),
				result.count()
		);
	}
}