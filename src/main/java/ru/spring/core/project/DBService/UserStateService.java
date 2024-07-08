package ru.spring.core.project.DBService;

import org.springframework.stereotype.Service;
import ru.spring.core.project.entity.UserState;

@Service
public interface UserStateService {
    public UserState addUserState(UserState userState);
    public UserState updateUserState(UserState userState);

}
