package ru.spring.core.project.DBService.impl;

import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.spring.core.project.DBService.PlaceService;
import ru.spring.core.project.entity.Place;
import ru.spring.core.project.entity.User;
import ru.spring.core.project.repositories.PlaceRepository;

import java.util.List;
@Component
public class PlaceServiceImpl implements PlaceService {
    @Autowired
    private PlaceRepository placeRepository;

    @Override
    public Place addPlace(Place place){
        Place savedPlace=placeRepository.saveAndFlush(place);
        return savedPlace;
    }

    @Override
    public Place updatePlace(Place place){
        Place updatedPlace =placeRepository.save(place);
        return updatedPlace;
    }

    @Override
    public List<Place> getAll(){
        return placeRepository.findAll();
    }

    @Override
    public List<Place> getAllLinkedUserByChatId(Long chatId){
        return placeRepository.findAllPlacesByChatId(chatId);
    }

    @Override
    public List<Place> getAllPlacesByPlaceName(String placeName){
        List<Place> returned =placeRepository.findAllPlacesByPlaceName(placeName);
        Hibernate.initialize(returned);
        return returned;
    }
}
