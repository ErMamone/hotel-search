package org.meroz.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Resultado del conteo de una busqueda existente")
public record CountResponseDTO(

		@Schema(description = "Hash SHA-256 a 32 caracteres que identifica la busqueda", example = "591a70d443c2383e881c87925cb86fde")
		String searchId,

		@Schema(description = "Datos originales de la busqueda")
		SearchPayload search,

		@Schema(description = "Cantidad de veces que se registro esta misma busqueda", example = "3")
		long count
) {

	@Schema(description = "Snapshot de los parametros de la busqueda")
	public record SearchPayload(

			@Schema(description = "ID alfanumerico del hotel", example = "RIU0001")
			String hotelId,

			@Schema(description = "Fecha de check-in", example = "19/05/2026", type = "string")
			@JsonFormat(pattern = "dd/MM/yyyy") LocalDate checkIn,

			@Schema(description = "Fecha de check-out", example = "26/05/2026", type = "string")
			@JsonFormat(pattern = "dd/MM/yyyy") LocalDate checkOut,

			@Schema(description = "Edades de los huespedes (el orden importa)", example = "[30, 29, 1, 3]")
			List<Integer> ages
	) {
		public SearchPayload {
			ages = List.copyOf(ages);
		}
	}
}
