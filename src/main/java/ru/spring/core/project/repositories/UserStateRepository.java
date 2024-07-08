package ru.spring.core.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.spring.core.project.entity.User;
import ru.spring.core.project.entity.UserState;

@Repository
public interface UserStateRepository extends JpaRepository<UserState,Long> {

}
