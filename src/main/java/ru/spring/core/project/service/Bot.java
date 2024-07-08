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
import ru.spring.core.project.DBService.impl.UserStateServiceImpl;
import ru.spring.core.project.entity.UserState;
import ru.spring.core.project.weatherCommunication.RequesterDataFromDBOrOpenWeatherMap;
import ru.spring.core.project.DBService.impl.PlaceServiceImpl;
import ru.spring.core.project.DBService.impl.UserServiceImpl;
import ru.spring.core.project.DBService.impl.WeatherDataImpl;
import ru.spring.core.project.config.BotConfig;
import ru.spring.core.project.entity.Place;
import ru.spring.core.project.entity.User;
import ru.spring.core.project.entity.WeatherData;
import ru.spring.core.project.weatherCommunication.CoordinateForWeatherBot;
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
    private ClassPathXmlApplicationContext context;
    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private PlaceServiceImpl placeService;

    @Autowired
    private WeatherDataImpl wheatherDataService;

    @Autowired
    private UserStateServiceImpl userStateService;
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
        Long chatId = update.getMessage().getChatId();
        List<User> listOfUsers = userService.getUsersByChatId(chatId);
        BotState currentState;
        if(listOfUsers.isEmpty()){// первое сообщение от пользователя
            currentState = BotState.BEFORE_START;
        }
        else{ // не первое
            currentState = listOfUsers.get(0).getUserState().getCurrentState();
        }

        logger.info("Обновление в состоянии : "+currentState.toString()+
                        " Пришло сообщение:" +update.getMessage().getText(),
                chatId );
        if(update.hasMessage() && update.getMessage().hasText()){
            handleMessege(update,currentState);
        }
        else if (update.getMessage().hasLocation()) {
            handleLocation(update,currentState);
        }
        else{
            errorAfterStartHandle(update,2);
        }
    }
    //Реализация Конечного автомата
    private void handleMessege(Update update,BotState currentState){
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
                    errorAfterStartHandle(update,0);
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
                    errorAfterStartHandle(update,0);
                    break;

            }
        }
    }


    private void handleLocation(Update update,BotState currentState){
        Location location = update.getMessage().getLocation();
        if(currentState==BotState.AFTER_START){
            correctReceiveLocationHandle(update);
        }
        else{
            errorAfterStartHandle(update,1);
        }
    }

    //Обработка полученных сообщений
    private void startHandle(Update update) {
        long chatId = update.getMessage().getChatId();
        String userName = update.getMessage().getFrom().getFirstName();
        String response = "Привет, " + userName + "!";
        sendMessage(chatId, response);
        sendInfoAfterStartMessege(chatId);
        User newUser= new User(userName,chatId);
        UserState userState = new UserState(newUser,BotState.AFTER_START);
        userService.addUserIfNotExistByChatId(newUser);
        userStateService.addUserState(userState);
    }

    private void clearHandle(Update update){
        long chatId = update.getMessage().getChatId();
        User currentUser;
        try {
            currentUser = userService.getUsersByChatId(chatId).get(0);
        }
        catch (Exception e){
            sendMessage(chatId,"Ошибка на стороне сервера. Не можем найти запись о Вас в базе данных");
            return;
        }
        UserState userState = currentUser.getUserState();
        String response ="Вы уверены, что хотите очистить все данные о вас?"+
                "\n/yes"+
                "\n/no";
        sendMessage(chatId, response);
        userState.setCurrentState(BotState.START_CLEARING);
        userStateService.updateUserState(userState);
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
        String response = "*Функционал бота:*\n\n"+
                "Для выполнения запроса нужно отправить название города или локацию.\n"+
                "После этого будет предложено выбрать опцию показа погоды\n\n"+
                "*Доступен просмотр погоды на периоды:*\n"+
                "Сейчас\n"+
                "Сегодня\n"+
                "На два дня\n"+
                "На три дня\n"+
                "На пять дней\n\n"+
                "*Кроме этого можно:*\n"+
                "Нажать /start - чтобы поздороваться;\n"+
                "Нажать /show - чтобы вывести списком все сохраненные города;\n"+
                "Нажать /clear - чтобы очистить сохраненные данные;\n"+
                "Нажать /help - чтобы показать это сообщение.";
        sendMessage(chatId,response);
    }
    private void cityNameHandle(Update update){
        long chatId = update.getMessage().getChatId();
        String messege = update.getMessage().getText();
        if(messege.charAt(0)=='/'){
            messege = messege.substring(1);
        }
        String response;
        User currentUser;
        try {
            currentUser = userService.getUsersByChatId(chatId).get(0);
        }
        catch (Exception e){
            sendMessage(chatId,"Ошибка на стороне сервера. Не можем найти запись о Вас в базе данных");
            return;
        }

        try{
            Place currentPlace = requesterDataFromDBOrOpenWeatherMap.getPlaceIfExist(messege);
            currentUser.addNewPlace(currentPlace);
            UserState userState = currentUser.getUserState();
            userState.setCurrentPlace(currentPlace);
            userState.setCurrentState(BotState.SENT_CITY_NAME_OR_LOCATION);
            userService.updateUser(currentUser);
            userStateService.updateUserState(userState);
            response = "Выберете период:\n" +
                    "/now\n"+
                    "/today\n" +
                    "/2days\n" +
                    "/3days\n" +
                    "/5days\n";
        }catch (Exception e){
            response = MessageFormat.format("Города {0} нет в нашей базе(", messege);
        }
        sendMessage(chatId,response);
    }

    private void forecastNowHandle(Update update){
        long chatId = update.getMessage().getChatId();
        User currentUser;
        try {
            currentUser = userService.getUsersByChatId(chatId).get(0);
        }
        catch (Exception e){
            sendMessage(chatId,"Ошибка на стороне сервера. Не можем найти запись о Вас в базе данных");
            return;
        }
        UserState userState = currentUser.getUserState();
        Place currentPlace = userState.getCurrentPlace();
        try {

            WeatherData weatherData = requesterDataFromDBOrOpenWeatherMap.getWeatherDataByPlaceNow(currentPlace);

            String response = responseWeatherForecastGenerator.getResponseBySingleWeatherData(weatherData);
            sendMessage(chatId,response);
        }
        catch (Exception e){
            String response ="Неизвестная ошибка на стороне сервера. Попробуйте заново.";
            sendMessage(chatId,response);
        }
        userState.setCurrentState(BotState.AFTER_START);
        userStateService.updateUserState(userState);
    }

    // эта функция отличается от предыдущей только вызовом NDaysForecast и ResponseByListOfWeatherData
    private void forecastNDayHandle(Update update, int amountDays){
        long chatId = update.getMessage().getChatId();
        User currentUser;
        try {
            currentUser = userService.getUsersByChatId(chatId).get(0);
        }
        catch (Exception e){
            sendMessage(chatId,"Ошибка на стороне сервера. Не можем найти запись о Вас в базе данных");
            return;
        }
        UserState userState = currentUser.getUserState();
        Place currentPlace = userState.getCurrentPlace();

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
        userState.setCurrentState(BotState.AFTER_START);
        userStateService.updateUserState(userState);
    }


    private void clearYesHandle(Update update){
        long chatId = update.getMessage().getChatId();
        User currentUser;
        try {
            currentUser = userService.getUsersByChatId(chatId).get(0);
        }
        catch (Exception e){
            sendMessage(chatId,"Ошибка на стороне сервера. Не можем найти запись о Вас в базе данных");
            return;
        }
        UserState userState = currentUser.getUserState();
        userState.setCurrentState(BotState.AFTER_START);
        userStateService.updateUserState(userState);
        String response = "";
        if(currentUser.getListOfPlaces()== null || currentUser.getListOfPlaces().isEmpty()){
            response = "У вас пока нет сохраненных мест.";
            sendMessage(chatId,response);
            return ;
        }
        currentUser = userService.deleteAllLinksWithPlaces(currentUser);
        //currentUser = userService.updateUser(currentUser);                             // если так делать была ошибка
        userService.updateUser(currentUser);
        response = "Ваша история поиска очищена.";
        sendMessage(chatId,response);
    }
    private void clearNoHandle(Update update){
        long chatId = update.getMessage().getChatId();
        User currentUser;
        try {
            currentUser = userService.getUsersByChatId(chatId).get(0);
        }
        catch (Exception e){
            sendMessage(chatId,"Ошибка на стороне сервера. Не можем найти запись о Вас в базе данных");
            return;
        }
        UserState userState = currentUser.getUserState();
        userState.setCurrentState(BotState.AFTER_START);
        userStateService.updateUserState(userState);

        String response = "Окей, не очищаем.";
        sendMessage(chatId,response);
    }
    private void correctReceiveLocationHandle(Update update){
        long chatId = update.getMessage().getChatId();
        User currentUser;
        try {
            currentUser = userService.getUsersByChatId(chatId).get(0);
        }
        catch (Exception e){
            sendMessage(chatId,"Ошибка на стороне сервера. Не можем найти запись о Вас в базе данных");
            return;
        }
        double latitude = update.getMessage().getLocation().getLatitude();
        double longitude = update.getMessage().getLocation().getLongitude();
        CoordinateForWeatherBot coordinateForWeatherBot = new CoordinateForWeatherBot(latitude,longitude);
        String response = Double.toString(latitude)+" "+Double.toString(longitude);
        sendMessage(chatId,response);
        response = "Выберете период:\n" +
                "/now\n"+
                "/today\n" +
                "/2days\n" +
                "/3days\n" +
                "/5days\n";
        sendMessage(chatId,response);
        UserState userState = currentUser.getUserState();
        Place currentPlace = new Place(coordinateForWeatherBot);
        userState.setCurrentPlace(currentPlace);
        userState.setCurrentState(BotState.SENT_CITY_NAME_OR_LOCATION);
        userStateService.updateUserState(userState);
    }

    //Сообщения об ошибках
    private void errorBeforeStartHandle(Update update){
        long chatId = update.getMessage().getChatId();
        String response ="Нажмите /start чтобы начать общение.";
        sendMessage(chatId, response);
    }
    /*
    * option = 0 - введена некорректная опция
    * option = 1 - ошибка при обработке локации
    * option = 2 - полученно некорректное сообщение(например фото)*/
    private void errorAfterStartHandle(Update update, int option){
        long chatId = update.getMessage().getChatId();
        User currentUser;
        try {
            currentUser = userService.getUsersByChatId(chatId).get(0);
        }
        catch (Exception e){
            sendMessage(chatId,"Ошибка на стороне сервера. Не можем найти запись о Вас в базе данных");
            return;
        }
        UserState userState = currentUser.getUserState();
        String response ="";
        switch (option){
            case 0:
                response ="Вы ввели некорректную опцию. Начните заново.";
                break;
            case 1:
                response = "Ошибка при обработке локации. Отправка локации не ожидается в этом состоянии.";
                break;
            case 2:
                response = "Получено некорректное сообщение. Попробуйте заново.";
                break;
            default:
                response = "Ошибка при обработке ошибок на стороне сервера) Попробуйте заново.";
                break;
        }
        sendMessage(chatId, response);
        sendInfoAfterStartMessege(chatId);
        userState.setCurrentState(BotState.AFTER_START);
        userStateService.updateUserState(userState);

    }


    //Отправка сообщений
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

}
