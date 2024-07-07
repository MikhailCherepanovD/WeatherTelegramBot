package ru.spring.core.project.DBService;

import ru.spring.core.project.entity.Place;

import java.util.List;

public interface PlaceService {
    Place addPlace(Place place);
    Place updatePlace(Place place);
    void deleteById(Long id);
    void deleteByCityName(String cityName);
    void deleteAllLinksUserByChatId(Long chatId);
    List<Place> getAll();
    List<Place> getAllLinkedUserByChatId(Long chatId);
    List<Place> getAllPlacesByPlaceName(String placeName);
}
