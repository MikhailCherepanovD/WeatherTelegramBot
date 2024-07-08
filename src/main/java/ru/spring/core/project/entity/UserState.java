package ru.spring.core.project.entity;

import jakarta.persistence.*;
import ru.spring.core.project.service.BotState;

import java.util.Objects;

@Entity
@Table(name = "USER_STATE")
public class UserState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "CURRENT_STATE")
    private BotState currentState;

    @OneToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "PLACE_ID", nullable = true)
    private Place currentPlace;

    public long getId() { return id; }

    public void setId(long id) { this.id = id; }

    public BotState getCurrentState() { return currentState; }

    public void setCurrentState(BotState currentState) { this.currentState = currentState; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    public Place getCurrentPlace() { return currentPlace; }

    public void setCurrentPlace(Place currentPlace) { this.currentPlace = currentPlace; }

    public UserState() { }

    public UserState(User user) { this.user = user; }
    public UserState(User user,BotState botState) {
        this.user = user;
        this.currentState = botState;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserState userState = (UserState) o;
        return id == userState.id && currentState == userState.currentState && Objects.equals(user, userState.user) && Objects.equals(currentPlace, userState.currentPlace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, currentState, user, currentPlace);
    }

    @Override
    public String toString() {
        return "UserState{" +
                "id=" + id +
                ", currentState=" + currentState +
                ", user=" + user +
                ", currentPlace=" + currentPlace +
                '}';
    }
}
