package ru.spring.core.project.entity;

import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name = "USERS")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "CHAT_ID")
    private long chatId;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.REMOVE}, fetch = FetchType.EAGER)       //Merge - нужен для того чтобы уже сохраненные объекты не сохранялись заново, иначе ALL
    @JoinTable(
            name = "USER_PLACE",
            joinColumns = @JoinColumn(name = "USER_ID"),
            inverseJoinColumns = @JoinColumn(name = "PLACE_ID")
    )
    List<Place> listOfPlaces;


    public List<Place> getListOfPlaces() {
        return listOfPlaces;
    }

    public void setListOfPlaces(List<Place> listOfPlaces) {
        this.listOfPlaces = listOfPlaces;
    }
    public long getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public Long getChatId() {
        return chatId;
    }
    public void setId(long id) {
        this.id = id;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public User(){

    }
    public User(String userName,Long chatId){
        this.userName=userName;
        this.chatId=chatId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && Objects.equals(userName, user.userName) && Objects.equals(chatId, user.chatId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userName, chatId);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", chatId='" + chatId + '\'' +
                '}';
    }
    public void addNewPlace(Place place){
        if(listOfPlaces ==null)
            listOfPlaces = new ArrayList<>();
        if(listOfPlaces.contains(place)){
            return ;
        }
        listOfPlaces.add(place);
        place.addUser(this);
    }
    public void removePlace(Place place){
        listOfPlaces.remove(place);
        place.removeUser(this);
    }
    public void removeAllPlaces(){
        if(listOfPlaces.isEmpty())
            return;
        for( Place place : listOfPlaces){
            place.removeUser(this);
        }
        listOfPlaces.clear();
    }
}
