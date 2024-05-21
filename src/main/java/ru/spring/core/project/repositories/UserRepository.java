package ru.spring.core.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.spring.core.project.entity.User;
@Repository
public interface UserRepository extends JpaRepository<User,Long> {

}
