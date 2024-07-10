# Telegram Weather Bot
This bot provide the opportunaty to find out the weather by city name or location.


## Bot's commands

/start  -  send welcome message, save the user in database;

/help  - send help message;

/show - show list of saved places by user;

/clear - clear list of places by user;

Apart from:

#### After sending city name or location bot provide to choise weather forecast option:

/now 

/today

/2days

/3days

/5days



## Implementation features

### Database

Database implement using PostgreSQL and Spring Data JPA

Scheme of database:

![](https://github.com/MikhailCherepanovD/WeatherBotRepository/blob/master/GitHubResources/schemaDB.png)




#### To reduce the number of accesses to the OpenWeatherMap api:
 
 The weather forecast for the current day is saved in the database. Then, if the weather forecast for the requested period is in the database, it is taken from there.

#### To reduce the number of accesses to the database:

The N last users are saved in LRUCache based on LinkedHashMap;

#### To reduce waiting time:

Updating users in the database after updating in the cache occurs in distinct threads.



### Testing 

Implemented classes:


[SingleUserTest](https://github.com/MikhailCherepanovD/WeatherBotRepository/blob/master/src/test/java/ru/spring/core/project/SingleUserTest.java)  - Modulating a single user scenario;


[MultiUserTest](https://github.com/MikhailCherepanovD/WeatherBotRepository/blob/master/src/test/java/ru/spring/core/project/MultiUserTest.java)  - Modulating the scenario of ten users, each of whom watches the weather in ten places;


Results of tests:

[MultiUserTest](https://github.com/MikhailCherepanovD/WeatherBotRepository/blob/master/src/test/java/ru/spring/core/project/TestLog/MultiUserTest.log) 

[SingleUserTest](https://github.com/MikhailCherepanovD/WeatherBotRepository/blob/master/src/test/java/ru/spring/core/project/TestLog/SingleUserTest.log) 






## How it work:

By city name:

<img src="https://github.com/MikhailCherepanovD/WeatherBotRepository/blob/master/GitHubResources/TelegramBotCommunication.gif" alt="" width="500"/>

By location:


<img src="https://github.com/MikhailCherepanovD/WeatherBotRepository/blob/master/GitHubResources/sentLocation.jpg" alt="" width="500"/>
