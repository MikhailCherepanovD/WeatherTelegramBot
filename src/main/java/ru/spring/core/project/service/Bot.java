package ru.spring.core.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.spring.core.project.DBService.impl.PlaceServiceImpl;
import ru.spring.core.project.DBService.impl.UserServiceImpl;
import ru.spring.core.project.DBService.impl.WeatherDataImpl;
import ru.spring.core.project.config.BotConfig;
import ru.spring.core.project.entity.User;
import ru.spring.core.project.repositories.PlaceRepository;
import ru.spring.core.project.repositories.UserRepository;
import ru.spring.core.project.repositories.WeatherDataRepository;
import ru.spring.core.project.statemachine.BotState;
import ru.spring.core.project.entity.WeatherData;
import ru.spring.core.project.weatherCommunication.WeatherRequestHandler;



import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


@Service
public class Bot extends TelegramLongPollingBot {

    @Autowired
    private final BotConfig config;
    @Autowired
    private WeatherRequestHandler weatherRequestHandler;
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);
    private BotState currentState = BotState.START;
    private User currentUser;
    private ClassPathXmlApplicationContext context;
    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private PlaceServiceImpl placeService;

    @Autowired
    private WeatherDataImpl wheatherDataService;

    //сравниваются введенные сообщения
    private final Map<String, Consumer<Update>> stateStartTransition = Map.of(

            "/start", this::startHandle,
            "/help", this::helpHendle,
            "/weather", this::weatherHendle,
            "/show", this::showHendle,
            "/clear", this::clearHendle

    );


    //сравниваются введенные сообщения из правого меню
    private final Map<String, Consumer<Update>> weatherStateTransition = Map.of(
            "Current Weather", this::handleLocationOrCity,
            "Full Weather Forecast", this::handleFullWeatherForecast
    );

    // тоже сравниваются введенные сообщения
    private final Map<String, Consumer<Update>> handleForecastedMode = Map.of(
            "The weather forecast for the day", update -> handleForecastSelection(update, 0),
            "The weather forecast for two days", update -> handleForecastSelection(update, 1),
            "Five-day weather forecast", update -> handleForecastSelection(update, 4),
            "Exit", this::handleExit
    );


    private final Map<String, Consumer<Update>> handleLocationOrCityAction = Map.of(
            "Type City Name1", this::handleCityNameInput,
            "Send Location", this::handleLocationInput,
            "Exit", this::handleExit
    );



    @Autowired


    // указываются сообщения в левом меню, главный конструктор бота
    public Bot(ClassPathXmlApplicationContext context) {
        this.config = context.getBean(BotConfig.class);
        this.context = context;

        weatherRequestHandler = context.getBean(WeatherRequestHandler.class);// = new WeatherRequestHandler(config);
        List<BotCommand> listOfCommands = new ArrayList();
        listOfCommands.add(new BotCommand("/start", "get a welcome message"));
        listOfCommands.add(new BotCommand("/help", "show help message"));
        listOfCommands.add(new BotCommand("/weather", "сhoosing a scenario option"));
        listOfCommands.add(new BotCommand("/show", "Show all my cities"));
        listOfCommands.add(new BotCommand("/clear", "Clear my list of cities"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
            logger.info("Create menu bot");
        } catch (TelegramApiException e) {
            logger.error("Error setting bot commands", e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {   // переходы по состояниям КА
        if(update.hasMessage() && update.getMessage().hasText()){
            long chatId = update.getMessage().getChatId();
            String message = update.getMessage().getText();
            String userName = update.getMessage().getFrom().getFirstName();

        }
    }


    private void handleExit(Update update) {
        changeBotState(BotState.START);
    }

   // меняет состояние бота
    private void changeBotState(BotState newState) {
        logger.info("Bot state changed to {}", newState);
        this.currentState = newState;
    }

  //пустой метод
    private void handleLocation(Location location, long chatId) {


    }


    private void handleFullWeatherForecast(Update update) {

    }


    private void handleForecastSelection(Update update, int forecastDays) {

    }


    private void helpHendle(Update update) {
        sendMessage(update.getMessage().getChatId(), "help");
    }


    private User startHandle(Update update) {
        long chatId = update.getMessage().getChatId();
        String userName = update.getMessage().getFrom().getFirstName();
        String response = "Hello " + userName + ". I provide an opportunity to find out the weather anywhere in the world at any time.";
        logger.info("Replied to user. response: " + response + " chatId: " + chatId + " name: " + userName);
        sendMessage(chatId, response);
        User newUser= new User(userName,chatId);
        return userService.addUserIfNotExistByChatId(newUser);

    }

    private void weatherHendle(Update update) {
        sendMessage(update.getMessage().getChatId(), "weather");
    }

    private void showHendle(Update update) {
        sendMessage(update.getMessage().getChatId(), "show");
    }

    private void clearHendle(Update update) {
        sendMessage(update.getMessage().getChatId(), "clear");
    }

    private void handleCityNameInput(Update update) {

    }


    private void sendWeatherData(long chatId, ArrayList<WeatherData> weatherDataList) {

    }




    private void handleLocationInput(Update update){

    }


    public void sendMessage(long chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(message);

        try {
            execute(sendMessage);
            logger.info("Message sent to user {}", chatId); // Добавляем лог об отправке сообщения пользователю
        } catch (TelegramApiException e) {
            logger.error("Error sending message to user {}", chatId, e); // Логируем ошибку, если не удалось отправить сообщение
        }
    }


    private void handleLocationOrCity(Update update) {

    }

    /**
     * Sends a message to the user with options for weather information and updates the bot state to current weather enter city.
     * @param update The update received from Telegram API containing the user message.
     */



    private void getWeatherCity(Update update) {

    }


    private void getForecastCity(long chatId, String cityName, int days) throws Exception {

    }

}
