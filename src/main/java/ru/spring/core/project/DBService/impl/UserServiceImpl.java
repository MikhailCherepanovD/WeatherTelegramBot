package ru.spring.core.project.DBService.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.spring.core.project.DBService.PlaceService;
import ru.spring.core.project.DBService.UserService;
import ru.spring.core.project.entity.Place;
import ru.spring.core.project.entity.User;
import ru.spring.core.project.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;
@Component
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    PlaceService placeService;

    @Override
    public User addUserIfNotExistByChatId(User user){
        List<User> users = getUsersByChatId(user.getChatId());
        if(!users.isEmpty()){
            return users.get(0);
        }
        User savedUser =userRepository.saveAndFlush(user);
        return savedUser;
    }
    @Override
    public User updateUser(User user){
        User updatedUser =userRepository.save(user);
        return updatedUser;
    }

    @Override
    public void deleteUserById(Long id){
        userRepository.deleteById(id);
    }

    @Override
    public void deleteUserByChatId(Long chatId){
        userRepository.deleteByChatId(chatId);
    }

    @Override
    public List<User> getAll(){
        return userRepository.findAll();
    }

    @Override
    public List<User> getUsersByChatId(Long chatId){
        return userRepository.findAllUsersByChatId(chatId);
    }
    @Override
    public  User deleteAllLinksWithPlaces(User user){
        if(user.getListOfPlaces().isEmpty()){
            return user;
        }
        List<Place> listOfPlace =new ArrayList<>(user.getListOfPlaces());
        user.removeAllPlaces();
        for(Place place : listOfPlace){
            placeService.updatePlace(place);
        }
        return user;
    }
}
