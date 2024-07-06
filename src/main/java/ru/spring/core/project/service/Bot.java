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
import ru.spring.core.project.BLusingBD.RequesterDataFromDBOrOpenWeatherMap;
import ru.spring.core.project.DBService.impl.PlaceServiceImpl;
import ru.spring.core.project.DBService.impl.UserServiceImpl;
import ru.spring.core.project.DBService.impl.WeatherDataImpl;
import ru.spring.core.project.config.BotConfig;
import ru.spring.core.project.entity.Place;
import ru.spring.core.project.entity.User;
import ru.spring.core.project.entity.WeatherData;
import ru.spring.core.project.statemachine.BotState;
import ru.spring.core.project.weatherCommunication.WeatherRequestHandler;
import ru.spring.core.project.utils.ResponseWeatherForecastGenerator;


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;


@Service
public class Bot extends TelegramLongPollingBot {

    @Autowired
    private final BotConfig config;
    @Autowired
    private WeatherRequestHandler weatherRequestHandler;
    @Autowired
    private RequesterDataFromDBOrOpenWeatherMap requesterDataFromDBOrOpenWeatherMap;
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);
    private BotState currentState = BotState.BEFORE_START;
    private User currentUser = new User();
    private Place currentPlace = new Place();
    private ClassPathXmlApplicationContext context;
    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private PlaceServiceImpl placeService;

    @Autowired
    private WeatherDataImpl wheatherDataService;
    @Autowired
    private ResponseWeatherForecastGenerator responseWeatherForecastGenerator;


    @Autowired
    // указываются сообщения в левом меню, главный конструктор бота
    public Bot(ClassPathXmlApplicationContext context) {
        this.config = context.getBean(BotConfig.class);
        this.context = context;

        weatherRequestHandler = context.getBean(WeatherRequestHandler.class);// = new WeatherRequestHandler(config);
        List<BotCommand> listOfCommands = new ArrayList();
        listOfCommands.add(new BotCommand("/start", "get a welcome message"));
        listOfCommands.add(new BotCommand("/help", "show help message"));
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
        logger.info("Обновление в состоянии : "+currentState.toString()+
                        " Пришло сообщение:" +update.getMessage().getText(),
                update.getMessage().getChatId() );
        if(update.hasMessage() && update.getMessage().hasText()){
            handleMessege(update);
        }
        else if (update.getMessage().hasLocation()) {
            handleLocation(update);
        }
        else{

        }
    }

    private void handleMessege(Update update){
        String message = update.getMessage().getText();
        if(currentState==BotState.BEFORE_START){
            switch (message){
                case "/start":
                    startHandle(update);
                    break;
                default:
                    errorBeforeStartHandle(update);
                    break;
            }
        }
        else if(currentState==BotState.AFTER_START){
            switch (message){
                case "/clear":
                    clearHandle(update);
                    break;
                case "/show":
                    showHandle(update);
                    break;
                case "/help":
                    helpHandle(update);
                    break;
                case "/start":
                    startHandle(update);
                    break;
                default:
                    cityNameHandle(update);
                    break;
            }
        }

        else if(currentState==BotState.SENT_CITY_NAME_OR_LOCATION){
            switch (message){
                case "/now":
                    forecastNowHandle(update);
                    break;
                case "/today":
                    forecastNDayHandle(update,0);
                    break;
                case "/2days":
                    forecastNDayHandle(update,1);
                    break;
                case "/3days":
                    forecastNDayHandle(update,2);
                    break;
                case "/5days":
                    forecastNDayHandle(update,4);
                    break;
                default:
                    errorInputIncorrectOptionHandle(update);
                    break;
            }
        }
        else if(currentState==BotState.START_CLEARING){
            switch (message) {
                case "/yes":
                    clearYesHandle(update);
                    break;
                case "/no":
                    clearNoHandle(update);
                    break;
                default:
                    errorInputIncorrectOptionHandle(update);
                    break;

            }
        }
    }


    private void handleLocation(Update update){
        Location location = update.getMessage().getLocation();
        if(currentState==BotState.AFTER_START){
            //correctReceiveLocationHandle(update);
        }
        else{
            //errorIncorrectReceiveLocationHandle(update);
        }
    }






    private void startHandle(Update update) {
        long chatId = update.getMessage().getChatId();
        String userName = update.getMessage().getFrom().getFirstName();
        String response = "Привет, " + userName + "!";
        sendMessage(chatId, response);
        sendInfoAfterStartMessege(chatId);
        User newUser= new User(userName,chatId);
        currentUser = userService.addUserIfNotExistByChatId(newUser);
        changeBotState(BotState.AFTER_START);
    }
    private void errorBeforeStartHandle(Update update){
        long chatId = update.getMessage().getChatId();
        String response ="Нажмите /start чтобы начать общение.";
        sendMessage(chatId, response);
    }
    private void errorInputIncorrectOptionHandle(Update update){
        long chatId = update.getMessage().getChatId();
        String response ="Вы ввели некорректную опцию. Начните заново.";
        sendMessage(chatId, response);
        sendInfoAfterStartMessege(chatId);
        changeBotState(BotState.AFTER_START);

    }
    private void clearHandle(Update update){
        long chatId = update.getMessage().getChatId();
        String response ="Вы уверены, что хотите очистить все данные о вас?"+
                "\n/yes"+
                "\n/no";
        sendMessage(chatId, response);
        changeBotState(BotState.START_CLEARING);
    }
    private void showHandle(Update update){
        long chatId = update.getMessage().getChatId();
        List<Place>listPlaces = placeService.getAllLinkedUserByChatId(chatId);
        String response="";
        if(listPlaces.isEmpty()){
            response+="У вас пока нет ни одного сохраненного места.";
        }
        else {
            for (Place p: listPlaces){
                response+="/";
                response+=p.getPlaceName();
                response+="\n";
            }
        }
        sendMessage(chatId,response);
    }
    private void helpHandle(Update update){
        long chatId = update.getMessage().getChatId();
        String response = "*Функционал бота:*\n"+
                "__Для выполнения запроса нужно отправить название города или локацию__.\n "+
                "~После этого будет предложено выбрать опцию показа погоды~"+
                "_Доступен просмотр погоды на периоды:_\n"+
                "Сейчас\n"+
                "Сегодня\n"+
                "На два дня\n"+
                "На три дня\n"+
                "На пять дней\n"+
                "Кроме этого доступно:\n"+
                "Нажать /start - чтобы поздороваться;\n"+
                "Нажать /show - чтобы вывести списком все сохраненные города;\n"+
                "Нажать /clear - чтобы очистить сохраненные данные;\n"+
                "Нажать /help - чтобы показать это сообщение";
        sendMessage(chatId,response);
    }
    private void cityNameHandle(Update update){
        long chatId = update.getMessage().getChatId();
        String messege = update.getMessage().getText();
        if(messege.charAt(0)=='/'){
            messege = messege.substring(1);
        }
        String response;
        try{
            currentPlace = requesterDataFromDBOrOpenWeatherMap.getPlaceIfExist(messege);
            currentUser.addNewPlace(currentPlace);
            userService.updateUser(currentUser);
            response = "Выберете период:\n" +
                    "/now\n"+
                    "/today\n" +
                    "/2days\n" +
                    "/3days\n" +
                    "/5days\n";
            changeBotState(BotState.SENT_CITY_NAME_OR_LOCATION);
        }catch (Exception e){
            response = MessageFormat.format("Города {0} нет в нашей базе(", messege);
        }
        sendMessage(chatId,response);
    }

    private void forecastNowHandle(Update update){
        long chatId = update.getMessage().getChatId();
        try {

            WeatherData weatherData = requesterDataFromDBOrOpenWeatherMap.getWeatherDataByPlaceNow(currentPlace);

            String response = responseWeatherForecastGenerator.getResponseBySingleWeatherData(weatherData);
            sendMessage(chatId,response);
        }
        catch (Exception e){
            String response ="Неизвестная ошибка на стороне сервера. Попробуйте заново.";
            sendMessage(chatId,response);
        }
        changeBotState(BotState.AFTER_START);
    }
    private void forecastNDayHandle(Update update, int amountDays){
        long chatId = update.getMessage().getChatId();
        String response1 = "N = "+(amountDays+1);

        try {
            List<WeatherData> listOfWeatherData = requesterDataFromDBOrOpenWeatherMap.getWeatherDataByPlaceNDay(currentPlace,amountDays);
            List<String> listOfMessage = responseWeatherForecastGenerator.getResponseByListOfWeatherData(listOfWeatherData);
            for(String str: listOfMessage){
                sendMessage(chatId,str);
            }
        }
        catch (Exception e){
            String response ="Неизвестная ошибка на стороне сервера. Попробуйте заново.";
            sendMessage(chatId,response);
        }
        sendMessage(chatId,response1);
        changeBotState(BotState.AFTER_START);
    }


    private void clearYesHandle(Update update){
        long chatId = update.getMessage().getChatId();
        String response = "Сейчас очистим.";
        sendMessage(chatId,response);
        placeService.deleteAllLinkedUserByChatId(chatId);
        response = "Ваша история поиска очищена.";
        sendMessage(chatId,response);
        changeBotState(BotState.AFTER_START);
    }
    private void clearNoHandle(Update update){
        long chatId = update.getMessage().getChatId();
        String response = "Окей, не очищаем.";
        sendMessage(chatId,response);
        changeBotState(BotState.AFTER_START);
    }
    private void sendInfoAfterStartMessege(long chatId){
        String response= "Отправьте мне название города или локацию, чтобы узнать погоду."+
                "\n **Чтобы посмотреть, что я умею**  - нажмите /help.";
        sendMessage(chatId,response);
    };








    private void sendMessage(long chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(message);
        sendMessage.enableMarkdown(true);
        try {
            execute(sendMessage);
            logger.info("Message sent to user {}", chatId); // Добавляем лог об отправке сообщения пользователю
        } catch (TelegramApiException e) {
            logger.error("Error sending message to user {}", chatId, e); // Логируем ошибку, если не удалось отправить сообщение
        }
    }


    private void changeBotState(BotState newState) {
        logger.info("Bot state changed to {}", newState);
        this.currentState = newState;
    }



}
