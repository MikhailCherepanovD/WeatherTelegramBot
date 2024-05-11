package ru.spring.core.project.service;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.spring.core.project.config.BotConfig;
import ru.spring.core.project.weatherCommunication.WeatherRequestHandler;



import java.util.ArrayList;
import java.util.List;




@Component
public class Bot extends TelegramLongPollingBot {

   private final BotConfig config;
   private WeatherRequestHandler weatherRequestHandler;
   static final String HELP_MESSAGE = "This bot идет нахуй потому что тут писать еще нечего";
   private static final Logger logger = LoggerFactory.getLogger(Bot.class);
   private BotState currentState = BotState.START;

   public Bot(BotConfig config) {
       this.config = config;
       weatherRequestHandler = new WeatherRequestHandler(config);
       logger.info("Bot initialized");
       List<BotCommand> listOfCommands = new ArrayList();
       listOfCommands.add(new BotCommand("/start", "get a welcome message"));
       listOfCommands.add(new BotCommand("/help", "show help message"));
       listOfCommands.add(new BotCommand("/weather", "name city"));

       try {
           this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
           logger.info( "Create menu bot");
       } catch (TelegramApiException e){
           logger.error( "Error setting bot commands", e.getMessage());
       }
   }

    private enum BotState{
        START,
        REQUEST_WEATHER_OPTION,
        REQUEST_LOCATION_OR_CITY,
        REQUEST_FULL_WEATHER_FORECAST,
        FULL_REQUEST_LOCATION_OR_CITY,
        REQUEST_LOCATION,
        REQUEST_CITY,
        FULL_REQUEST_LOCATION,
        FULL_REQUEST_CITY
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    public String getOpenWeatherMapKey(){
       return config.getOpenWeatherMapKey();
    }

    private void changeBotState(BotState newState) {
        logger.info("Bot state changed to {}", newState);
        this.currentState = newState;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            long chatId = update.getMessage().getChatId();
            if (update.getMessage().hasText()) {
                String message = update.getMessage().getText();
                logger.info("Received update from user");
                switch (currentState) {
                    case START:
                        switch (message) {
                            case "/start":
                                startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                                break;
                            case "/help":
                                sendMessage(chatId, HELP_MESSAGE);
                                break;
                            case "/weather":
                                changeBotState(BotState.REQUEST_WEATHER_OPTION);
                                requestWeatherOptions(chatId);
                                break;
                            default:
                                sendMessage(chatId, "The command is not recognized. Try again or use /help");
                        }
                        break;
                    case REQUEST_WEATHER_OPTION:
                        switch (message) {
                            case "Current Weather":
                                changeBotState(BotState.REQUEST_LOCATION_OR_CITY);
                                requestLocationOrCity(chatId);
                                break;
                            case "Full weather forecast":
                                changeBotState(BotState.REQUEST_FULL_WEATHER_FORECAST);
                                sendMessage(chatId, "You can find out the forecast for the \n /current_day_forecast \n /tomorrow_forecast \n /5_days_ahead");
                                break;
                            default:


                        }
                        break;
                    case REQUEST_LOCATION_OR_CITY:
                        switch (message) {
                            case "Send Location":
                                changeBotState(BotState.REQUEST_LOCATION);
                                requestLocationOrCity(chatId);
                                break;
                            case "Type City Name":
                                changeBotState(BotState.REQUEST_CITY);
                                sendMessage(chatId, "Please type the name of the city.");
                                break;
                            case "exit":
                                changeBotState(BotState.REQUEST_WEATHER_OPTION);
                                requestWeatherOptions(chatId);
                                break;
                            default:
                                //getWeatherCity(chatId, message);
                        }
                        break;
                    case REQUEST_FULL_WEATHER_FORECAST:
                        switch (message){
                            case "/current_day_forecast":
                                changeBotState(BotState.FULL_REQUEST_LOCATION_OR_CITY);
                                requestLocationOrCity(chatId);
                                break;
                            case "/tomorrow_forecast":
                                break;
                            case "/5_days_ahead":
                                break;
                            default:


                        }
                        break;
                    case FULL_REQUEST_LOCATION_OR_CITY:
                        switch (message){
                            case "Send Location":
                                changeBotState(BotState.FULL_REQUEST_LOCATION);
                                requestLocationOrCity(chatId);
                                break;
                            case "Type City Name":
                                changeBotState(BotState.FULL_REQUEST_CITY);
                                sendMessage(chatId, "Please type the name of the city.");
                                break;
                            case "exit":
                                changeBotState(BotState.REQUEST_WEATHER_OPTION);
                                requestWeatherOptions(chatId);
                                break;
                            default:
                        }
                        break;
                    case REQUEST_LOCATION:
                        // Обработка сообщений в состоянии REQUEST_LOCATION
                        // Ваша логика для обработки локации
                        break;
                    case REQUEST_CITY:
                        // Обработка сообщений в состоянии REQUEST_CITY
                        // Ваша логика для обработки введенного города
                        break;
                    case FULL_REQUEST_LOCATION:
                        // Обработка сообщений в состоянии REQUEST_LOCATION
                        // Ваша логика для обработки локации
                        break;
                    case FULL_REQUEST_CITY:
                        try {
                            getForecastCity(chatId, message);
                        } catch (Exception e) {
                            logger.error("Error getting forecast city", e);
                        }
                        break;
                }
            } else if(update.getMessage().hasLocation()) {
                // Обработка полученной локации
            } else {
                sendMessage(chatId, "Sorry, your message was not recognized. Please try again or type /help for assistance.");
            }
        }
    }


//    @Override
//    public void onUpdateReceived(Update update) {
//        if (update.hasMessage()) {
//            long chatId = update.getMessage().getChatId();
//            if (update.getMessage().hasText()) {
//                String message = update.getMessage().getText();
//                switch (message){
//                    case "/start":
//                        startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
//                        break;
//                    case "/help":
//                        sendMessage(chatId, HELP_MESSAGE);
//                        break;
//                    case "Send Location":
//                        requestLocationOrCity(chatId);
//                        break;
//                    case "Type City Name":
//                        sendMessage(chatId,"Please type the name of the city.");
//                        break;
//                    case "/weather":
//                        requestWeatherOptions(chatId);
//                        break;
//                    case "Current Weather":
//                        requestLocationOrCity(chatId);
//                        break;
//                    case "exit":
//                        requestWeatherOptions(chatId);
//                        break;
//                    case "Full weather forecast":
//                        sendMessage(chatId, "You can find out the forecast for the \n /current_day_forecast \n /tomorrow_forecast \n /5_days_ahead");
//                        break;
//                    case "/current_day_forecast":
//                        requestLocationOrCity(chatId);
//                        break;
//                    case "/tomorrow_forecast":
//                        requestLocationOrCity(chatId);
//                        break;
//                    case "/5_days_ahead":
//                        requestLocationOrCity(chatId);
//                        break;
//                    default:
//                        getWeatherCity(chatId, message);
//                }
//            } else if(update.getMessage().hasLocation()) {
//                Location location = update.getMessage().getLocation();
//                handleLocation(location, chatId);
//            }else{
//                sendMessage(chatId, "Sorry, your message was not recognized. Please try again or type /help for assistance.");
//            }
//        }
//    }

    private void handleLocation(Location location, Long chatId) {
       sendMessage(chatId, "Thank you for using the bot. We will start finding your city.");
       Double latitude = location.getLatitude();
       Double longitude = location.getLongitude();
       SendMessage message = new SendMessage();
       message.setChatId(chatId);

       try {
           var response = weatherRequestHandler.getWeatherDataCoordinatesNow(latitude,longitude);
           String weather = response.weatherResponse();
           message.setText(weather);
           execute(message);
           logger.info("Send message to user {0}", chatId);
       } catch (TelegramApiException e) {
           logger.error("Error sending location", e.getMessage());
       } catch (Exception e) {
           throw new RuntimeException(e);
       }
    }

    private void startCommandReceived (long chatId, String name) {
        String response = "Hello " + name + ". I provide an opportunity to find out the weather anywhere in the world at any time.";
       // log.info("Replied to user. response: " + response + " chatId: " + chatId + " name: " + name);
        sendMessage(chatId, response);
    }

    private void sendMessage(long chatId, String message) {
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

    private void requestLocationOrCity(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("How would you like to provide your location?");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        KeyboardButton buttonLocation = new KeyboardButton();
        buttonLocation.setText("Send Location");
        buttonLocation.setRequestLocation(true);
        row.add(buttonLocation);

        KeyboardButton buttonCity = new KeyboardButton();
        buttonCity.setText("Type City Name");
        row.add(buttonCity);
        keyboard.add(row);

        row = new KeyboardRow();
        KeyboardButton buttonExit= new KeyboardButton();
        buttonExit.setText("exit");
        row.add(buttonExit);
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
           logger.error("Error sending message to user {}", chatId, e);
        }
    }

    private void requestWeatherOptions(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Please select the weather option:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        KeyboardButton buttonCurrentWeather = new KeyboardButton();
        buttonCurrentWeather.setText("Current Weather");
        row.add(buttonCurrentWeather);
        keyboard.add(row);

        row = new KeyboardRow();
        KeyboardButton buttonTomorrowWeather = new KeyboardButton();
        buttonTomorrowWeather.setText("Full weather forecast");
        row.add(buttonTomorrowWeather);
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.error("Error sending message to user {}", chatId, e);
        }
    }

    private void getWeatherCity(long chatId, String cityName) {
       String response = weatherRequestHandler.getAnswerCityNowReturnString(cityName);
       SendMessage sendMessage = new SendMessage();
       sendMessage.setChatId(String.valueOf(chatId));
       sendMessage.setText(response);

       try {
           execute(sendMessage);
           //ArrayList<WeatherData> arrayList = weatherRequestHandler.getResponseCityNDay(cityName,4);
       } catch (TelegramApiException e) {
           logger.error("Error requesting location", e);
       } catch (Exception e) {
           throw new RuntimeException(e);
       }
    }

    private void getForecastCity(long chatId, String cityName) throws Exception {
        var  response = weatherRequestHandler.getResponseCityNDay(cityName, 0);
        System.out.println(response);
//        SendMessage sendMessage = new SendMessage();
//        sendMessage.setChatId(String.valueOf(chatId));

    }
}
