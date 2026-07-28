package com.mtbs.catalog;

import com.mtbs.catalog.domain.City;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<City, Long> {
}
