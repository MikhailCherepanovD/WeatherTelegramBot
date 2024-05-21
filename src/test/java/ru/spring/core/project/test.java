package ru.spring.core.project;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.spring.core.project.entity.Place;
import ru.spring.core.project.entity.User;
import ru.spring.core.project.entity.WeatherData;
import ru.spring.core.project.repositories.PlaceRepository;
import ru.spring.core.project.repositories.UserRepository;
import ru.spring.core.project.repositories.WeatherDataRepository;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
public class test {
    @Test
    public void testFunction1() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/applicationContext.xml");
        PlaceRepository placeReprository = context.getBean(PlaceRepository.class);
        UserRepository userReprository = context.getBean(UserRepository.class);
        WeatherDataRepository weatherDataReprository = context.getBean(WeatherDataRepository.class);

        Place placeSPB =new Place();
        placeSPB.setPlaceName("Piter");
        placeSPB.setLongitude(12);
        placeSPB.setLongitude(13);

        Place placeChelyabinsk =new Place();
        placeChelyabinsk.setPlaceName("Chelyba");
        placeChelyabinsk.setLongitude(1);
        placeChelyabinsk.setLongitude(1);



        User userKevGen =new User();
        userKevGen.setUserName("KevGenBot517265w1726575576576");
        userKevGen.setChatId(65L);
        Set<Place> setOfPlaces = new HashSet<>();
        setOfPlaces.add(placeSPB);
        setOfPlaces.add(placeChelyabinsk);
        userKevGen.setSetOfPlaces(setOfPlaces);


        User userCherep =new User();
        userCherep.setUserName("Cherep124535623e2735e723e23yuw fxvjwdx ");
        userCherep.setChatId(65L);
        setOfPlaces = new HashSet<>();
        setOfPlaces.add(placeSPB);
        setOfPlaces.add(placeChelyabinsk);
        userKevGen.setSetOfPlaces(setOfPlaces);

        WeatherData weatherDataCh1=new WeatherData();
        weatherDataCh1.setPlace(placeChelyabinsk);
        weatherDataCh1.setTemperature(-110);

        WeatherData weatherDataCh2=new WeatherData();
        weatherDataCh2.setPlace(placeChelyabinsk);
        weatherDataCh2.setTemperature(-100);

        WeatherData weatherDataSPB1=new WeatherData();
        weatherDataSPB1.setPlace(placeSPB);
        weatherDataSPB1.setTemperature(210);

        WeatherData weatherDataSPB2=new WeatherData();
        weatherDataSPB2.setPlace(placeSPB);
        weatherDataSPB2.setTemperature(310);



        userReprository.save(userCherep);
        userReprository.save(userKevGen);

        placeReprository.save(placeSPB);
        placeReprository.save(placeChelyabinsk);

        weatherDataReprository.save(weatherDataCh1);
        weatherDataReprository.save(weatherDataCh2);

        weatherDataReprository.save(weatherDataSPB1);
        weatherDataReprository.save(weatherDataSPB2);
    }
}
