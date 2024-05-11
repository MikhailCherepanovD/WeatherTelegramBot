package ru.spring.core.project.weatherCommunication;

import com.github.prominence.openweathermap.api.OpenWeatherMapClient;
import com.github.prominence.openweathermap.api.enums.Language;
import com.github.prominence.openweathermap.api.enums.UnitSystem;
import com.github.prominence.openweathermap.api.model.forecast.WeatherForecast;
import com.github.prominence.openweathermap.api.model.weather.Weather;

import java.time.LocalDate;
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




    // этот метод оставил для теста
    String ParseWeatherReturnString(Weather parseWeather){
        String ans=" Now:\n"+
                Math.round(parseWeather.getTemperature().getValue())+"\n"
                +parseWeather.getWeatherState()+"\n"
                +parseWeather.getAtmosphericPressure()+"\n"
                +parseWeather.getHumidity()+"\n";
        return ans;
    };
    // этот метод оставил для теста
    String ParseForecastReturnString(WeatherForecast parseWeather){
        String ans=parseWeather.getForecastTimeISO()+"\n"
                +Math.round(parseWeather.getTemperature().getValue())+"\n"
                +parseWeather.getWeatherState()+"\n"
                +parseWeather.getAtmosphericPressure()+"\n"
                +parseWeather.getHumidity()+"\n";
        return ans;
    };





    // этот метод оставил для теста
    public String GetAnswerCityNowReturnString(String cityName){
        try {
            Weather currentWeather = openWeatherClient
                    .currentWeather()
                    .single()
                    .byCityName(cityName)
                    .language(Language.RUSSIAN)
                    .unitSystem(UnitSystem.METRIC)
                    .retrieve()
                    .asJava();
            String ans = ParseWeatherReturnString(currentWeather);
            return ans;
        }
        catch(Exception e){
            System.out.println("Произошло исключение: " + e);
            return "error";
        }
    };
    // этот метод оставил для теста
    public String GetAnswerCityTodayReturnString(String cityName){
        try {
            List<WeatherForecast> listForecast = openWeatherClient.forecast5Day3HourStep()
                    .byCityName(cityName).language(Language.RUSSIAN)
                    .unitSystem(UnitSystem.METRIC)
                    .count(8)
                    .retrieve().asJava().getWeatherForecasts();
            String ans = "Сегодня: \n";
            for (int i = 0; i < listForecast.size(); i++) {
                LocalTime now = LocalTime.now();
                if (listForecast.get(i).getForecastTime().toLocalTime().isAfter(now)) {
                    break;
                }
                ans += listForecast.get(i).getForecastTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")) + " ";
                ans += ParseForecastReturnString(listForecast.get(i));
            }
            //String ans=ParseWeather(listForecast.get(i).getWeatherState());
            return ans;
        }
        catch(Exception e){
            System.out.println("Произошло исключение: " + e);
            return "error";
        }
    };
    WeatherData parseWeather(Weather currentWeather, String cityName){
        WeatherData weatherData= new WeatherData();
        weatherData.setCityName(cityName);
        weatherData.setDate(LocalDate.now());
        weatherData.setTime(LocalTime.now());
        weatherData.setDay(LocalDate.now().getDayOfWeek());
        weatherData.setTemperature(Math.round((float)currentWeather.getTemperature().getValue()));
        weatherData.setHumidity((float)currentWeather.getHumidity().getValue());
        weatherData.setPressure((float)currentWeather.getAtmosphericPressure().getValue()); // нужно перевести в мм рт ст
        weatherData.setWindSpeed((float)currentWeather.getWind().getSpeed());
        weatherData.setWeatherStateMain(currentWeather.getWeatherState().getName());
        weatherData.setWeatherStateDiscription(currentWeather.getWeatherState().getDescription());
        return weatherData;
    }
    public String GetAnswerCityNow(String cityName) {
        Weather currentWeather = openWeatherClient
                .currentWeather()
                .single()
                .byCityName(cityName)
                .language(Language.RUSSIAN)
                .unitSystem(UnitSystem.METRIC)
                .retrieve()
                .asJava();

    }


}
