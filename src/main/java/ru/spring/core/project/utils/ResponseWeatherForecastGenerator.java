package ru.spring.core.project.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import ru.spring.core.project.entity.WeatherData;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class ResponseWeatherForecastGenerator {
    @Autowired
    private ApplicationContext context;
    @Autowired
    public ResponseWeatherForecastGenerator(){}

    public List<String> getResponseByListOfWeatherData(List<WeatherData> listOfWeatherData){
        List<String> ls = new ArrayList<>();
        String header = "*Погода в городе "+listOfWeatherData.get(0).getPlace().getPlaceName()+":*";
        ls.add(header);
        for(int i =0;i<listOfWeatherData.size();i++) {
            LocalDate currentDate = listOfWeatherData.get(i).getDate();
            String strSingleDate="*"+currentDate.format(DateTimeFormatter.ofPattern("MMMM-dd"))+"*\n\n\n";

            while ( i<listOfWeatherData.size() && listOfWeatherData.get(i).getDate().equals(currentDate)){
                strSingleDate+=listOfWeatherData.get(i).getTime().format(DateTimeFormatter.ofPattern("HH:mm"))+"\n";
                strSingleDate+=getOnlyWeatherDataBySingleTime(listOfWeatherData.get(i))+"\n\n";
                i++;
            }
            ls.add(strSingleDate);
        }
        return ls;
    }

    public String getResponseBySingleWeatherData(WeatherData weatherData){
        StringBuilder answer = new StringBuilder("");
        answer.append("*Погода в городе ");
        answer.append(weatherData.getPlace().getPlaceName());
        answer.append(" сейчас:*");

        answer.append("\n\n*Температура:* ");
        answer.append(weatherData.getTemperature());
        answer.append(" \u00B0C");

        answer.append("\n*Скорость ветра:* ");
        answer.append(weatherData.getWindSpeed());
        answer.append(" м/с");

        answer.append("\n*Давление:* ");
        answer.append(transformFromPascalToMM(weatherData.getPressure()));
        answer.append(" мм. рт. ст.");

        answer.append("\n\n");
        answer.append(weatherData.getWeatherStateDescription().substring(0,1).toUpperCase()+weatherData.getWeatherStateDescription().substring(1));

        return answer.toString();
    }
    private String  transformFromPascalToMM(float number){
        number*=100;
        number*=0.007501;
        return String.valueOf((int)number);
    }


    private String getOnlyWeatherDataBySingleTime(WeatherData weatherData){
        StringBuilder answer = new StringBuilder("");
        answer.append("*Температура:* ");
        answer.append(weatherData.getTemperature());
        answer.append(" \u00B0C");

        answer.append("\n*Скорость ветра:* ");
        answer.append(weatherData.getWindSpeed());
        answer.append(" м/с");

        answer.append("\n*Давление:* ");
        answer.append(transformFromPascalToMM(weatherData.getPressure()));
        answer.append(" мм. рт. ст.");

        answer.append("\n");
        answer.append(weatherData.getWeatherStateDescription().substring(0,1).toUpperCase()+weatherData.getWeatherStateDescription().substring(1));

        return answer.toString();
    }
}
