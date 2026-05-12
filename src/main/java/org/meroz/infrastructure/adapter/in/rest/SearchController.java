package org.meroz.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.meroz.application.port.in.CountSearchesUseCase;
import org.meroz.application.port.in.CreateSearchCommand;
import org.meroz.application.port.in.CreateSearchUseCase;
import org.meroz.domain.model.HotelSearch;
import org.meroz.infrastructure.adapter.in.rest.dto.CountResponseDTO;
import org.meroz.infrastructure.adapter.in.rest.dto.SearchRequestDTO;
import org.meroz.infrastructure.adapter.in.rest.dto.SearchResponseDTO;
import org.meroz.infrastructure.adapter.in.rest.mapper.HotelSearchMapper;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@Tag(name = "Hotel Search", description = "Busquedas de disponibilidad y conteo")
public class SearchController {

	private final CreateSearchUseCase createSearch;

	private final CountSearchesUseCase countSearch;

	private final HotelSearchMapper hotelSearchMapper;

	@PostMapping("/search")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(
			summary = "Registra una busqueda de disponibilidad",
			description = "Persiste la busqueda, publica un evento Kafka y devuelve el searchId deterministico"
	)
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Busqueda creada"),
			@ApiResponse(responseCode = "400", description = "Datos invalidos",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "503", description = "Fallo publicando evento")
	})
	public SearchResponseDTO search(@Valid @RequestBody SearchRequestDTO request) {
		var command = new CreateSearchCommand(request.hotelId(), request.checkIn(), request.checkOut(), request.ages());
		HotelSearch result = createSearch.createSearch(command);
		return new SearchResponseDTO(result.searchId());
	}

	@GetMapping("/count")
	@Operation(summary = "Cuenta cuantas veces se realizo una busqueda")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Conteo encontrado"),
			@ApiResponse(responseCode = "404", description = "searchId no existe")
	})
	public CountResponseDTO count(@Parameter(description = "Hash SHA-256 truncado de 32 caracteres",
			example = "591a70d443c2383e881c87925cb86fde")
								  @RequestParam String searchId) {
		return hotelSearchMapper.toCountResponse(countSearch.countBySearchId(searchId));
	}
}
