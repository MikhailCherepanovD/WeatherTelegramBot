package ru.spring.core.project.weatherCommunication;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
// объектами этого класса будем передавать данные между классами
public class WeatherData {

    String cityName;
    LocalTime time;
    LocalDate date;
    DayOfWeek day;
    int temperature;
    float windSpeed;
    float humidity; // влажность
    float pressure;
    String weatherStateMain;// облачно, ясно, пасмурно ...
    String weatherStateDescription; // доп описание к этому
    double latitude;
    double longitude;

    public String getCityName() {
        return cityName;
    }

    public LocalTime getTime() {
        return time;
    }

    public LocalDate getDate() {
        return date;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public int getTemperature() {
        return temperature;
    }

    public float getWindSpeed() {
        return windSpeed;
    }

    public float getHumidity() {
        return humidity;
    }

    public float getPressure() {
        return pressure;
    }

    public String getWeatherStateMain() {
        return weatherStateMain;
    }

    public String getWeatherStateDescription() {
        return weatherStateDescription;
    }

    public double getLatitude() { return latitude; }

    public double getLongitude() { return longitude; }


    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setDay(DayOfWeek day) {
        this.day = day;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public void setWindSpeed(float windSpeed) {
        this.windSpeed = windSpeed;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    public void setWeatherStateMain(String weatherStateMain) {
        this.weatherStateMain = weatherStateMain;
    }

    public void setWeatherStateDescription(String weatherStateDescription) {
        this.weatherStateDescription = weatherStateDescription;
    }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String weatherResponse() {
        String response = "City: " + cityName
                + "\n" + "Latitude: " + latitude
                + "\n" + "Longitude: " + longitude
                + "\n" + "Date: " + date
                + "\n" + "Time: " + time
                + "\n" + "Day: " + day
                + "\n" + "Temperature: " + temperature
                + "\n" + "Wind speed: " + windSpeed
                + "\n" + "Humidity: " + humidity + "%"
                + "\n" + "Pressure: " + pressure
                + "\n" + "Weather conditions: " + weatherStateMain
                + "\n" + "Description of the weather: " + weatherStateDescription;
        return response;
    }


}