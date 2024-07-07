package ru.spring.core.project;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.transaction.Transactional;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.spring.core.project.weatherCommunication.RequesterDataFromDBOrOpenWeatherMap;
import ru.spring.core.project.DBService.impl.PlaceServiceImpl;
import ru.spring.core.project.DBService.impl.UserServiceImpl;
import ru.spring.core.project.DBService.impl.WeatherDataImpl;
import ru.spring.core.project.entity.Place;
import ru.spring.core.project.entity.User;
import ru.spring.core.project.entity.WeatherData;
import ru.spring.core.project.repositories.PlaceRepository;
import ru.spring.core.project.repositories.UserRepository;
import ru.spring.core.project.repositories.WeatherDataRepository;
import ru.spring.core.project.weatherCommunication.CoordinateForWeatherBot;
import ru.spring.core.project.weatherCommunication.WeatherRequestHandler;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class test {
    @Test
    public void testUsingRepositories() throws Exception{
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/applicationContext.xml");
        PlaceRepository placeRepository = context.getBean(PlaceRepository.class);
        UserRepository userRepository = context.getBean(UserRepository.class);
        WeatherDataRepository weatherDataRepository = context.getBean(WeatherDataRepository.class);

        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("jpaData");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        WeatherRequestHandler weatherRequestHandler = context.getBean(WeatherRequestHandler.class);


        User userKevGen = new User();
        userKevGen.setUserName("KevGen");
        userKevGen.setChatId(1L);
        userRepository.save(userKevGen);
        User userCherep = new User();
        userCherep.setUserName("Cherep");
        userCherep.setChatId(2L);
        userRepository.save(userCherep);
        userRepository.save(userCherep);
        userRepository.save(userCherep);


        Place placeSPB =new Place();
        placeSPB.setPlaceName("Санкт-Петербург");
        placeSPB.setCoordinate(new CoordinateForWeatherBot(123,12));
        placeRepository.save(placeSPB);


        Place placeChelyabinsk =new Place();
        placeChelyabinsk.setPlaceName("Челябинск");
        placeRepository.save(placeChelyabinsk);



        Place placeMoscow =new Place();
        placeMoscow.setPlaceName("Москва");
        placeRepository.save(placeMoscow);





        List<Place> listOfPlaces = new ArrayList<>();
        listOfPlaces.add(placeSPB);
        listOfPlaces.add(placeChelyabinsk);
        listOfPlaces.add(placeMoscow);
        userKevGen.setListOfPlaces(listOfPlaces);
        userCherep.setListOfPlaces(listOfPlaces);



        userRepository.save(userKevGen);
        userRepository.save(userCherep);
        entityManager.close();



        Set<WeatherData> weatherChelyabinsk = new HashSet<>(weatherRequestHandler.getWeatherDataByCityNameNDay("Челябинск", 1));
        Set<WeatherData> weatherSPB = new HashSet<>(weatherRequestHandler.getWeatherDataByCityNameNDay("Санкт-Петербург", 1));
        Set<WeatherData> weatherMoskow = new HashSet<>(weatherRequestHandler.getWeatherDataByCityNameNDay("Москва", 1));
        for(WeatherData wd: weatherChelyabinsk ){
            wd.setPlace(placeChelyabinsk);
            weatherDataRepository.save(wd);

        }
        for(WeatherData wd: weatherMoskow ){
            wd.setPlace(placeMoscow);
            weatherDataRepository.save(wd);
        }
        for(WeatherData wd: weatherSPB ){
            wd.setPlace(placeSPB);
            weatherDataRepository.save(wd);

        }
        LocalDate date = LocalDate.of(2024, 5, 25);
        LocalTime time =LocalTime.of(14, 30, 0);
        List<WeatherData> lwd = weatherDataRepository.findAllWeatherDataByPlaceAndDateAfterCurrentTime("Челябинск",time,date);
        for(WeatherData wd :lwd){
            System.out.println(wd.toString());
        }





    }
    @Test
    public void testDBService() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/applicationContext.xml");
        PlaceServiceImpl placeService = context.getBean(PlaceServiceImpl.class);
        UserServiceImpl userService = context.getBean(UserServiceImpl.class);
        WeatherDataImpl weatherDataService = context.getBean(WeatherDataImpl.class);
        RequesterDataFromDBOrOpenWeatherMap requesterDataFromDBOrOpenWeatherMap = context.getBean(RequesterDataFromDBOrOpenWeatherMap.class);

        User userKevGen = new User("KevGen",1L);
        userService.addUserIfNotExistByChatId(userKevGen);

        User userCherep = new User("Cherep",2L);


        Place placeSPB=new Place("Санкт-Петербург");
        Place placeMoscow=new Place("Москва");
        Place placeChelybinsk=new Place("Челябинск");
        placeService.addPlace(placeSPB);
        placeService.addPlace(placeChelybinsk);
        placeService.addPlace(placeMoscow);

        userKevGen.addNewPlace(placeSPB);
        userKevGen.addNewPlace(placeMoscow);
        userKevGen.addNewPlace(placeChelybinsk);

        userService.addUserIfNotExistByChatId(userKevGen);


        userCherep.addNewPlace(placeSPB);
        userCherep.addNewPlace(placeMoscow);
        userCherep.addNewPlace(placeChelybinsk);

        userService.addUserIfNotExistByChatId(userCherep);

        List<WeatherData> weatherChelyabinsk = new ArrayList<>(requesterDataFromDBOrOpenWeatherMap.getWeatherDataByPlaceNDay(placeChelybinsk, 5));
        List<WeatherData> weatherSPB = new ArrayList<>(requesterDataFromDBOrOpenWeatherMap.getWeatherDataByPlaceNDay(placeSPB, 5));
        List<WeatherData> weatherMoskow = new ArrayList<>(requesterDataFromDBOrOpenWeatherMap.getWeatherDataByPlaceNDay(placeMoscow, 5));

        for(WeatherData wd: weatherChelyabinsk ){
            wd.setPlace(placeChelybinsk);
            weatherDataService.addWeatherData(wd);
            System.out.println(wd.toString());

        }
        for(WeatherData wd: weatherMoskow ){
            wd.setPlace(placeMoscow);
            weatherDataService.addWeatherData(wd);
        }
        for(WeatherData wd: weatherSPB ){
            wd.setPlace(placeSPB);
            weatherDataService.addWeatherData(wd);
        }

        List<WeatherData> currWd = weatherDataService.getAllWeatherDataByPlaceAndDateNearCurrentTime("Челябинск", LocalTime.now(),LocalDate.now());
        System.out.println("Текущая погода в челябинске :\n\n\n\n "+ currWd.get(0).toString());


        List<Place> lp = placeService.getAllLinkedUserByChatId(userCherep.getChatId());
        for(Place p: lp){
            System.out.println(p.getPlaceName());
        }
        //placeService.deleteAllLinkedUserByChatId(2L);

    }

    @Test
    public void testWeatherRequestHandler() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/applicationContext.xml");
        WeatherRequestHandler weatherRequestHandler =context.getBean(WeatherRequestHandler.class);
        List<WeatherData> weatherSPB = weatherRequestHandler.getWeatherDataByCityNameNDay("Санкт-Петербург",0);
        for(WeatherData wd: weatherSPB ){
            System.out.println(wd.toString());
        }
    }

    @Test
    public void testRequesterDataFromDBOrOpenWeatherMap() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/applicationContext.xml");
        PlaceServiceImpl placeService = context.getBean(PlaceServiceImpl.class);
        UserServiceImpl userService = context.getBean(UserServiceImpl.class);
        RequesterDataFromDBOrOpenWeatherMap requesterDataFromDBOrOpenWeatherMap = context.getBean(RequesterDataFromDBOrOpenWeatherMap.class);

        User userKevGen = new User("KevGen",1L);
        userService.addUserIfNotExistByChatId(userKevGen);

        User userCherep = new User("Cherep",2L);


        Place placeSPB=new Place("Санкт-Петербург");
        Place placeMoscow=new Place("Москва");
        Place placeChelybinsk=new Place("Челябинск");
        placeService.addPlace(placeSPB);
        placeService.addPlace(placeChelybinsk);
        placeService.addPlace(placeMoscow);

        userKevGen.addNewPlace(placeSPB);
        userKevGen.addNewPlace(placeMoscow);
        userKevGen.addNewPlace(placeChelybinsk);

        userService.addUserIfNotExistByChatId(userKevGen);


        userCherep.addNewPlace(placeSPB);
        userCherep.addNewPlace(placeMoscow);
        userCherep.addNewPlace(placeChelybinsk);

        userService.addUserIfNotExistByChatId(userCherep);
        //requesterDataFromDBOrOpenWeatherMap.getWeatherDataByPlaceNDay(placeSPB,5);
        List<WeatherData> lwd =requesterDataFromDBOrOpenWeatherMap.getWeatherDataByPlaceNDay(placeSPB,0);
        for(WeatherData wd: lwd){
            System.out.println(wd.toString());
        }


    }





    @Test
    public void testGettingFromDB() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/applicationContext.xml");
        WeatherDataImpl weatherDataService = context.getBean(WeatherDataImpl.class);
        LocalDate currDate =  LocalDate.now();
        LocalTime currTime =  LocalTime.now();
        List<WeatherData> ans = new ArrayList<>();
        ans = weatherDataService.getAllWeatherDataByPlaceAndDateAfterCurrentTime("Санкт-Петербург",currTime,currDate);
        for(WeatherData wd: ans){
            System.out.println(wd.toString());
        }
    }
    @Test
    public void addSameIdPlace() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/applicationContext.xml");
        PlaceRepository placeRepository = context.getBean(PlaceRepository.class);
        UserRepository userRepository = context.getBean(UserRepository.class);
        WeatherDataRepository weatherDataRepository = context.getBean(WeatherDataRepository.class);
        UserServiceImpl userService = context.getBean(UserServiceImpl.class);
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("jpaData");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        WeatherRequestHandler weatherRequestHandler = context.getBean(WeatherRequestHandler.class);


        User userKevGen = new User();
        userKevGen.setUserName("KevGen");
        User userCherep = new User();
        userCherep.setUserName("Cherep");
        userCherep.setChatId(5L);
        userService.addUserIfNotExistByChatId(userCherep);
        userService.addUserIfNotExistByChatId(userKevGen);

    }
    @Test
    public void addIncorrectPlace()throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/applicationContext.xml");
        PlaceRepository placeRepository = context.getBean(PlaceRepository.class);
        UserRepository userRepository = context.getBean(UserRepository.class);
        WeatherDataRepository weatherDataRepository = context.getBean(WeatherDataRepository.class);
        UserServiceImpl userService = context.getBean(UserServiceImpl.class);
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("jpaData");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        WeatherRequestHandler weatherRequestHandler = context.getBean(WeatherRequestHandler.class);
        User userKevGen = new User();
        userKevGen.setUserName("KevGen");
        userService.addUserIfNotExistByChatId(userKevGen);
        Place placeSPB =new Place();
        placeSPB.setPlaceName("Санкт-Петербург");
        userKevGen.addNewPlace(placeSPB);
        //placeRepository.save(placeSPB);

    }
    @Test
    public void testGetPlaceIfExist()throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/applicationContext.xml");
        RequesterDataFromDBOrOpenWeatherMap requesterDataFromDBOrOpenWeatherMap =  context.getBean(RequesterDataFromDBOrOpenWeatherMap.class);
        try {
            Place place = requesterDataFromDBOrOpenWeatherMap.getPlaceIfExist("Москвkjkh555а");
            System.out.println("Нашли город "+place.getPlaceName());
        }catch (Exception e){
            System.out.println("Не нашли город ");
        }
    }





    @Test
    public void testAddBDUsingRepoN2() throws Exception{
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/applicationContext.xml");
        PlaceRepository placeRepository = context.getBean(PlaceRepository.class);
        UserRepository userRepository = context.getBean(UserRepository.class);

        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("jpaData");
        EntityManager entityManager = entityManagerFactory.createEntityManager();



        User userKevGen = new User();
        userKevGen.setUserName("KevGen");
        userKevGen.setChatId(1L);

        Place placeSPB =new Place();
        placeSPB.setPlaceName("Санкт-Петербург");


        userKevGen.addNewPlace(placeSPB);
        placeRepository.saveAndFlush(placeSPB);
        userRepository.saveAndFlush(userKevGen);

        entityManager.close();

    }




    @Test
    public void testAddUserAndPaceBD() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/applicationContext.xml");
        PlaceServiceImpl placeService = context.getBean(PlaceServiceImpl.class);
        UserServiceImpl userService = context.getBean(UserServiceImpl.class);

        User userKevGen = new User("KevGen",1L);
        User userCherep = new User("Cherep",2L);


        Place placeMSC=new Place("Москва");

        userCherep.addNewPlace(placeMSC);
        userKevGen.addNewPlace(placeMSC);
        placeService.addPlace(placeMSC);

        userService.addUserIfNotExistByChatId(userKevGen);
        userService.addUserIfNotExistByChatId(userCherep);

        List<Place> listPlaces = placeService.getAllPlacesByPlaceName("Москва");

        Place p0 = listPlaces.get(0);

        userCherep.addNewPlace(p0);
        userService.updateUser(userCherep);
        userCherep.addNewPlace(p0);
        userService.updateUser(userCherep);
        userCherep.addNewPlace(p0);
        p0.setPlaceName("Москва");
        userService.updateUser(userCherep);
        userCherep.addNewPlace(p0);
        userService.updateUser(userCherep);

    }
    @Transactional
    @Test
    public void testDeleteLinksUsingListsRemoving() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/applicationContext.xml");
        PlaceServiceImpl placeService = context.getBean(PlaceServiceImpl.class);
        UserServiceImpl userService = context.getBean(UserServiceImpl.class);

        User userKevGen = new User("KevGen",1L);
        Place placeMSC=new Place("Москва");

        userKevGen.addNewPlace(placeMSC);
        placeService.addPlace(placeMSC);
        userService.addUserIfNotExistByChatId(userKevGen);
        userKevGen.removePlace(placeMSC);
        int h=0;
        userService.updateUser(userKevGen);
        placeService.updatePlace(placeMSC);

    }

    @Transactional
    @Test
    public void testDeleteLinksUsingSQL() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/applicationContext.xml");
        PlaceServiceImpl placeService = context.getBean(PlaceServiceImpl.class);
        UserServiceImpl userService = context.getBean(UserServiceImpl.class);

        User userKevGen = new User("KevGen",1L);
        Place placeMSC=new Place("Москва");
        userKevGen.addNewPlace(placeMSC);
        placeService.addPlace(placeMSC);
        userService.addUserIfNotExistByChatId(userKevGen);
        userKevGen = userService.deleteAllLinksWithPlaces(userKevGen);
        userService.updateUser(userKevGen);

    }

    @Test
    public void testAddDublicatePlaces(){
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/applicationContext.xml");
        PlaceServiceImpl placeService = context.getBean(PlaceServiceImpl.class);
        UserServiceImpl userService = context.getBean(UserServiceImpl.class);

        RequesterDataFromDBOrOpenWeatherMap requesterDataFromDBOrOpenWeatherMap = context.getBean(RequesterDataFromDBOrOpenWeatherMap.class);

        User userKevGen = new User("KevGen",1L);
        userService.addUserIfNotExistByChatId(userKevGen);
        Place placeMSC=new Place("Москва");
        Place placeSPB=new Place("Санкт-Петербург");
        Place placeChelyaba = new Place("Челябинск");

        placeService.addPlace(placeMSC);
        placeService.addPlace(placeChelyaba);
        placeService.addPlace(placeSPB);

        userKevGen.addNewPlace(placeMSC);
        userKevGen.addNewPlace(placeSPB);
        userKevGen.addNewPlace(placeChelyaba);
        userService.updateUser(userKevGen);

        userKevGen= userService.deleteAllLinksWithPlaces(userKevGen);
        //userKevGen= userService.updateUser(userKevGen);                    // Если это оставить он работает некорректно
        try {
            placeMSC = requesterDataFromDBOrOpenWeatherMap.getPlaceIfExist("Москва");
            userKevGen.addNewPlace(placeMSC);
            userService.updateUser(userKevGen);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

        try {
            placeChelyaba = requesterDataFromDBOrOpenWeatherMap.getPlaceIfExist("Челябинск");
            userKevGen.addNewPlace(placeChelyaba);
            userService.updateUser(userKevGen);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        try {
            Place placePerm = requesterDataFromDBOrOpenWeatherMap.getPlaceIfExist("Пермь");
            userKevGen.addNewPlace(placePerm);
            userService.updateUser(userKevGen);
        }
        catch (Exception e){
                System.out.println(e.getMessage());
        }



        try {
            Place placePerm1 = requesterDataFromDBOrOpenWeatherMap.getPlaceIfExist("Пермь");
            userKevGen.addNewPlace(placePerm1);
            userService.updateUser(userKevGen);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

        try {
            Place placePerm2 = requesterDataFromDBOrOpenWeatherMap.getPlaceIfExist("Пермь");
            userKevGen.addNewPlace(placePerm2);
            userService.updateUser(userKevGen);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

        try {
            Place placePerm3 = requesterDataFromDBOrOpenWeatherMap.getPlaceIfExist("Пермь");
            userKevGen.addNewPlace(placePerm3);
            userService.updateUser(userKevGen);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }


    }

}
