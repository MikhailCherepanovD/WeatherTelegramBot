package ru.spring.core.project.entity;

import jakarta.persistence.*;
import ru.spring.core.project.weatherCommunication.CoordinateForWeatherBot;

import java.util.*;

@Entity
@Table(name = "PLACE")
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "PLACE_NAME")
    private String placeName;



    @OneToMany(fetch = FetchType.EAGER,mappedBy = "place", cascade = CascadeType.ALL )
    private List<WeatherData> listOfWeatherData;

    @ManyToMany(mappedBy = "listOfPlaces", fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    private List<User> listOfUser;

    @Embedded
    CoordinateForWeatherBot coordinate;

    public List<User> getListOfUser() {
        return listOfUser;
    }
    public List<WeatherData> getListOfWeatherData() { return listOfWeatherData; }

    public void setListOfWeatherData(List<WeatherData> listOfWeatherData) { this.listOfWeatherData = listOfWeatherData; }

    public void setListOfUser(List<User> listOfUser) {
        this.listOfUser = listOfUser;
    }
    public long getId() {
        return id;
    }

    public String getPlaceName() {
        return placeName;
    }


    public CoordinateForWeatherBot getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(CoordinateForWeatherBot coordinate) {
        this.coordinate = coordinate;
    }


    public void setId(long id) {
        this.id = id;
    }



    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }


    public Place(){

    }
    public Place(String placeName){
        this.placeName=placeName;
    }
    public Place(CoordinateForWeatherBot coordinateForWeatherBot){
        this.coordinate=coordinateForWeatherBot;
    }

    @Override
    public String toString() {
        return "Place{" +
                "id=" + id +
                ", placeName='" + placeName + '\'' +
                ", setOfWeatherData=" + listOfWeatherData +
                ", setOfUser=" + listOfUser +
                ", coordinate=" + coordinate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Place place = (Place) o;
        return Objects.equals(placeName, place.placeName); //&& Objects.equals(setOfWeatherData, place.setOfWeatherData) && Objects.equals(setOfUser, place.setOfUser) && Objects.equals(coordinate, place.coordinate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(placeName);//, setOfWeatherData, setOfUser, coordinate);
    }


    public void addWeatherData(List<WeatherData> listOfWeatherData){
        if(this.listOfWeatherData ==null)
            this.listOfWeatherData =new ArrayList<>();
        this.listOfWeatherData.addAll(listOfWeatherData);
    }
    public void addWeatherData(WeatherData weatherData){
        if(listOfWeatherData ==null)
            listOfWeatherData =new ArrayList<>();
        listOfWeatherData.add(weatherData);
    }
    public void addUser(User user){
        if(listOfUser ==null)
            listOfUser =new ArrayList<>();
        listOfUser.add(user);
    }
    protected void removeUser(User user){
        //setOfUser.remove(user);
        float hash1 = user.hashCode();
        float hash2;
        for(User user1 : listOfUser){
           if(user1.equals(user)){
               listOfUser.remove(user1);
           }
        }
        int h=1;

    }
}