# Telegram Weather Bot
This bot provide the opportunity to find out the weather by city name or location.

Gets the weather forecast from: OpenWeatherMap api;

## Bot's commands

/start  -  sends welcome message, save the user in the database;

/help  - sends a help message;

/show - shows list of saved places by the user;

/clear - clears list of places saved by the user;

Apart from:

#### After sending city name or location bot provides a choice of weather forecast option:

/now 

/today

/2days

/3days

/5days



## Implementation features

### Database

The database is implemented using PostgreSQL and Spring Data JPA

Scheme of the database:

![](https://github.com/MikhailCherepanovD/WeatherBotRepository/blob/master/GitHubResources/schemaDB.png)




#### To reduce the number of accesses to the OpenWeatherMap API:
 
 The weather forecast for the current day is saved in the database. Then, if the weather forecast for the requested period is in the database, it is taken from there.

#### To reduce the number of accesses to the database:

The last N users are saved in LRUCache based on LinkedHashMap;

#### To reduce waiting time:

Updating users in the database after updating in the cache occurs in distinct threads.



### Testing 

Implemented classes:


[SingleUserTest](https://github.com/MikhailCherepanovD/WeatherBotRepository/blob/master/src/test/java/ru/spring/core/project/SingleUserTest.java)  - Simulating a single user scenario;


[MultiUserTest](https://github.com/MikhailCherepanovD/WeatherBotRepository/blob/master/src/test/java/ru/spring/core/project/MultiUserTest.java)  - Simulating the scenario of ten users, each of whom watches the weather in ten places;


Results of tests:

[MultiUserTest](https://github.com/MikhailCherepanovD/WeatherBotRepository/blob/master/src/test/java/ru/spring/core/project/TestLog/MultiUserTest.log) 

[SingleUserTest](https://github.com/MikhailCherepanovD/WeatherBotRepository/blob/master/src/test/java/ru/spring/core/project/TestLog/SingleUserTest.log) 






## How it work:

#### By city name:

<img src="https://github.com/MikhailCherepanovD/WeatherBotRepository/blob/master/GitHubResources/TelegramBotCommunication.gif" alt="" width="500"/>

#### By location:


<img src="https://github.com/MikhailCherepanovD/WeatherBotRepository/blob/master/GitHubResources/sentLocation.jpg" alt="" width="500"/>
