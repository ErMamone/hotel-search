package org.meroz.infrastructure.adapter.in.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.meroz.application.mapper.HotelSearchMapper;
import org.meroz.domain.model.HotelSearch;
import org.meroz.domain.port.in.CountSearchesUseCase;
import org.meroz.domain.port.in.CreateSearchCommand;
import org.meroz.domain.port.in.CreateSearchUseCase;
import org.meroz.infrastructure.adapter.in.rest.dto.CountResponseDTO;
import org.meroz.infrastructure.adapter.in.rest.dto.SearchRequestDTO;
import org.meroz.infrastructure.adapter.in.rest.dto.SearchResponseDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SearchController {

	private final CreateSearchUseCase createSearch;

	private final CountSearchesUseCase countSearch;

	private final HotelSearchMapper hotelSearchMapper;

	@PostMapping("/search")
	public SearchResponseDTO search(@Valid @RequestBody SearchRequestDTO request) {
		var command = new CreateSearchCommand(request.hotelId(), request.checkIn(), request.checkOut(), request.ages());
		HotelSearch result = createSearch.createSearch(command);
		return new SearchResponseDTO(result.searchId());
	}

	@GetMapping("/count")
	public CountResponseDTO count(@RequestParam String searchId) {
		return hotelSearchMapper.toCountResponse(countSearch.countBySearchId(searchId));
	}
}
