package org.meroz.domain.port.out;

import org.meroz.domain.model.HotelSearch;

public interface SearchEventPublisherPort {
	void publish(HotelSearch hotelSearch);
}
