package ru.spring.core.project.DBService;

import jakarta.transaction.Transactional;
import ru.spring.core.project.entity.Place;

import java.util.List;

public interface PlaceService {
    Place addPlace(Place place);
    void deleteById(Long id);
    void deleteByCityName(String cityName);
    void deleteAllLinkedUserByChatId(Long chatId);
    List<Place> getAll();
    List<Place> getAllLinkedUserByChatId(Long chatId);
    List<Place> getAllPlacesByPlaceName(String placeName);
}
