package ru.spring.core.project.weatherCommunication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.prominence.openweathermap.api.OpenWeatherMapClient;
import com.github.prominence.openweathermap.api.enums.Language;
import com.github.prominence.openweathermap.api.enums.UnitSystem;
import com.github.prominence.openweathermap.api.mapper.CurrentWeatherResponseMapper;
import com.github.prominence.openweathermap.api.model.forecast.WeatherForecast;
import com.github.prominence.openweathermap.api.model.weather.Weather;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class WeatherRequestHandler {
    OpenWeatherMapClient openWeatherClient;
    public WeatherRequestHandler(){
        String API_TOKEN = "a8734f088878edc423890fec5b2a94e5";
        openWeatherClient = new OpenWeatherMapClient(API_TOKEN);
    }
    String GetAnswerCoord(){return "";};

    String ParseWeather(Weather parseWeather){
        String ans=" Now:\n"+
                Math.round(parseWeather.getTemperature().getValue())+"\n"
                +parseWeather.getWeatherState()+"\n"
                +parseWeather.getAtmosphericPressure()+"\n"
                +parseWeather.getHumidity()+"\n";
        return ans;
    };
    String ParseForecast(WeatherForecast parseWeather){
        String ans=parseWeather.getForecastTimeISO()+"\n"
                +Math.round(parseWeather.getTemperature().getValue())+"\n"
                +parseWeather.getWeatherState()+"\n"
                +parseWeather.getAtmosphericPressure()+"\n"
                +parseWeather.getHumidity()+"\n";
        return ans;
    };


    public String GetAnswerCityNow(String cityName){
        Weather currentWeather = openWeatherClient
                .currentWeather()
                .single()
                .byCityName("Норильск")
                .language(Language.RUSSIAN)
                .unitSystem(UnitSystem.METRIC)
                .retrieve()
                .asJava();
        String ans=ParseWeather(currentWeather);
        return ans;
    };
    public String GetAnswerCityToday(String cityName){
        List<WeatherForecast> listForecast  = openWeatherClient.forecast5Day3HourStep()
                .byCityName(cityName).language(Language.RUSSIAN)
                .unitSystem(UnitSystem.METRIC)
                .count(8)
                .retrieve().asJava().getWeatherForecasts();
        String ans="Сегодня: \n";
        for(int i = 0;i<listForecast.size();i++){
            LocalTime now = LocalTime.now();
            if( listForecast.get(i).getForecastTime().toLocalTime().isAfter(now) ){
                break;
            }
            ans+= listForecast.get(i).getForecastTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))+" ";
            ans+=ParseForecast(listForecast.get(i));
        }
        //String ans=ParseWeather(listForecast.get(i).getWeatherState());
        return ans;
    };
}
