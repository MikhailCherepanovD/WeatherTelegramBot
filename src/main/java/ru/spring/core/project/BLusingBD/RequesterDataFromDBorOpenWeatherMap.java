package ru.spring.core.project.BLusingBD;


import com.github.prominence.openweathermap.api.OpenWeatherMapClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import ru.spring.core.project.config.BotConfig;
import ru.spring.core.project.entity.WeatherData;
import ru.spring.core.project.weatherCommunication.WeatherRequestHandler;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.swing.text.AbstractDocument;
import java.util.ArrayList;
@Component
class RequesterDataFromDBorOpenWeatherMap{
    private WeatherRequestHandler weatherRequestHandler;
    private BotConfig config;

    @Autowired
    private ApplicationContext context;

    @Autowired
    public RequesterDataFromDBorOpenWeatherMap(BotConfig configuration) {
        weatherRequestHandler= new WeatherRequestHandler(config);
    }

    public WeatherData getWeatherDataCoordinatesNow(double latitude, double longitude) throws Exception {
        return weatherRequestHandler.getWeatherDataCoordinatesNow(latitude,longitude);
    }

    public WeatherData getWeatherDataCityNow(String cityName) throws Exception {
        //context.getBean()
    }

    public ArrayList<WeatherData> getResponseCityNDay(String cityName, int amountDays) throws Exception {

    }

    public ArrayList<WeatherData> getAnswerCoordsNDay(double latitude, double longitude, int amountDays) throws Exception {
        return weatherRequestHandler.getAnswerCoordsNDay(latitude, longitude, amountDays);
    }


}

