package org.meroz.domain.port.in;

import org.meroz.domain.model.HotelSearch;

public interface CreateSearchUseCase {

	HotelSearch createSearch(CreateSearchCommand command);

}
