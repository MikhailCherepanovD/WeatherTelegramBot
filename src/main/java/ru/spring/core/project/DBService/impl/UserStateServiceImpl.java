package ru.spring.core.project.DBService.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.spring.core.project.DBService.UserStateService;
import ru.spring.core.project.entity.UserState;
import ru.spring.core.project.repositories.UserStateRepository;

@Component
public class UserStateServiceImpl implements UserStateService {
    @Autowired
    UserStateRepository userStateRepository;
    @Override
    public  UserState addUserState(UserState userState){
        synchronized (this) {
            UserState savedUserState = userState = userStateRepository.saveAndFlush(userState);
            return savedUserState;
        }
    }

    @Override
    public UserState updateUserState(UserState userState){
        synchronized (this) {
            UserState savedUserState = userState = userStateRepository.save(userState);
            return savedUserState;
        }
    }
}
