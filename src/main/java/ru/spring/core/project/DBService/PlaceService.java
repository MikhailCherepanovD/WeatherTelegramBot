package ru.spring.core.project.DBService;

import ru.spring.core.project.entity.Place;

import java.util.List;

public interface PlaceService {
    Place addPlace(Place place);
    Place updatePlace(Place place);
    List<Place> getAll();
    List<Place> getAllLinkedUserByChatId(Long chatId);
    List<Place> getAllPlacesByPlaceName(String placeName);
}
