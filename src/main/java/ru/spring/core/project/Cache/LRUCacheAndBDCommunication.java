package ru.spring.core.project.Cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.spring.core.project.DBService.impl.PlaceServiceImpl;
import ru.spring.core.project.DBService.impl.UserServiceImpl;
import ru.spring.core.project.DBService.impl.UserStateServiceImpl;
import ru.spring.core.project.DBService.impl.WeatherDataServiceImpl;
import ru.spring.core.project.entity.User;

import java.util.List;

@Component
public class LRUCacheAndBDCommunication {
    @Autowired
    private PlaceServiceImpl placeService;
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private UserStateServiceImpl userStateService;
    @Autowired
    private WeatherDataServiceImpl weatherDataService;
    private LRUCache<Long,User>  lruCacheUsers;

    public LRUCacheAndBDCommunication(){
        lruCacheUsers = new LRUCache<>(3);
    }

    public User addUser(User user){
        lruCacheUsers.put(user.getChatId(),user);
        userService.addUserIfNotExistByChatId(user);
        userStateService.addUserState(user.getUserState());
        return user;
    }

    public User updateUser(User user){
        lruCacheUsers.put(user.getChatId(),user);
        userService.updateUser(user);
        userStateService.updateUserState(user.getUserState());
        return user;
    }
    public boolean userIsExist(Long chatId){
        if(lruCacheUsers.containsKey(chatId)){
            return true;
        }
        List<User> ls = userService.getUsersByChatId(chatId);
        if(ls.isEmpty()){
            return false;
        }
        else{
            User savingUser = ls.getFirst();
            lruCacheUsers.put(savingUser.getChatId(),savingUser);
            return true;
        }
    }

    public User getUserByChatId(Long chatId){
        if(lruCacheUsers.containsKey(chatId)){
            return lruCacheUsers.get(chatId);
        }
        List<User> ls = userService.getUsersByChatId(chatId);
        User savingUser = ls.getFirst();
        lruCacheUsers.put(savingUser.getChatId(),savingUser);
        return savingUser;
    }
    public void removeFromCache(User user){
        lruCacheUsers.remove(user.getChatId());
    }

}
