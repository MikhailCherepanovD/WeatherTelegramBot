package ru.spring.core.project.Cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import ru.spring.core.project.DBService.impl.PlaceServiceImpl;
import ru.spring.core.project.DBService.impl.UserServiceImpl;
import ru.spring.core.project.DBService.impl.UserStateServiceImpl;
import ru.spring.core.project.DBService.impl.WeatherDataServiceImpl;
import ru.spring.core.project.entity.User;

import java.util.List;

@Component
public class LRUCacheAndBDCommunication {
    private final int AMOUNT_CACHED_USERS=5;
    @Autowired
    private PlaceServiceImpl placeService;
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private UserStateServiceImpl userStateService;
    @Autowired
    private WeatherDataServiceImpl weatherDataService;
    private LRUCache<Long,User>  lruCacheUsers;
    @Autowired
    ClassPathXmlApplicationContext context;

    public LRUCacheAndBDCommunication(){
        lruCacheUsers = new LRUCache<>(AMOUNT_CACHED_USERS);
    }

    public User addUser(User user){
        lruCacheUsers.put(user.getChatId(),user);
        ThreadSave threadSave = new ThreadSave();
        threadSave.setUserService(userService);
        threadSave.setUser(user);
        threadSave.start();
        return user;
    }

    public User updateUser(User user){
        lruCacheUsers.put(user.getChatId(),user);
        ThreadUpdate threadUpdate = new ThreadUpdate();
        threadUpdate.setUserService(userService);
        threadUpdate.setUser(user);
        threadUpdate.start();
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
