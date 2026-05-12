package org.meroz.infrastructure.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SearchResponseDTO(
		@Schema(description = "ID deterministico de la busqueda", example = "591a70d443c2383e881c87925cb86fde")
		String searchId) {
}
