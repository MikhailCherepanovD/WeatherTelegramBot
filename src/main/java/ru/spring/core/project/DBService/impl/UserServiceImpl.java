package ru.spring.core.project.DBService.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.spring.core.project.DBService.UserService;
import ru.spring.core.project.entity.User;
import ru.spring.core.project.repositories.UserRepository;

import java.util.List;
@Component
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

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
}
