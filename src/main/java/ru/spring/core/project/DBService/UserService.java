package ru.spring.core.project.DBService;

import org.springframework.stereotype.Service;
import ru.spring.core.project.entity.User;

import java.util.List;

@Service
public interface UserService {
    User addUserIfNotExistByChatId(User user);
    User updateUser(User user);
    void deleteUserById(Long id);
    void deleteUserByChatId(Long chatId);
    User deleteAllLinksWithPlaces(User user);
    List<User> getAll();
    List<User> getUsersByChatId(Long chatId);


}
