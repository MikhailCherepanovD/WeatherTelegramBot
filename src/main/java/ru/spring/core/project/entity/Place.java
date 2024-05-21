package ru.spring.core.project.entity;

import jakarta.persistence.*;

import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "PLACE")
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "PLACE_NAME")
    private String placeName;

    @Column(name = "LATITUDE")
    float latitude;
    @Column(name = "LONGITUDE")
    float longitude;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<WeatherData> setOfWeatherData;

    @ManyToMany(mappedBy = "setOfPlaces")
    private Set<User> setOfUser;



    public Set<User> getSetOfUser() {
        return setOfUser;
    }
    public Set<WeatherData> getSetOfWeatherData() { return setOfWeatherData; }

    public void setSetOfWeatherData(Set<WeatherData> setOfWeatherData) { this.setOfWeatherData = setOfWeatherData; }

    public void setSetOfUser(Set<User> setOfUser) {
        this.setOfUser = setOfUser;
    }
    public long getId() {
        return id;
    }

    public String getPlaceName() {
        return placeName;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }


    public void setId(long id) {
        this.id = id;
    }



    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }
    public Place(){

    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Place place = (Place) o;
        return Float.compare(latitude, place.latitude) == 0 && Float.compare(longitude, place.longitude) == 0 && id == place.id && Objects.equals(placeName, place.placeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(placeName, latitude, longitude, id);
    }

    @Override
    public String toString() {
        return "Place{" +
                "placeName='" + placeName + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", id=" + id +
                '}';
    }
}