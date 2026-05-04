package org.meroz.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HotelSearchJpaRepository extends JpaRepository<HotelSearchEntity, Long> {
	long countBySearchId(String searchId);

	Optional<HotelSearchEntity> findFirstBySearchId(String searchId);
}
