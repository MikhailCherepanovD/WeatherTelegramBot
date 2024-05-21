package ru.spring.core.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.spring.core.project.entity.WeatherData;

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData,Long> {

}
