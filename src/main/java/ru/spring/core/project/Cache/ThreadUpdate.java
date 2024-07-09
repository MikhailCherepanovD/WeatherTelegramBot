package ru.spring.core.project.Cache;

import ru.spring.core.project.DBService.impl.UserServiceImpl;
import ru.spring.core.project.entity.User;

public class ThreadUpdate extends Thread{
    private UserServiceImpl userService;
    User user;

    public void setUser(User user) {
        this.user = user;
    }
    public void setUserService(UserServiceImpl userService) {
        this.userService = userService;
    }
    @Override
    public void run()
    {
        userService.updateUser(user);
        System.out.println("Пользователь "+user.getUserName()+" сохранен отдельным потоком.");
    }
}
