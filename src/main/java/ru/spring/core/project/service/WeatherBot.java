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
import ru.spring.core.project.Cache.LRUCacheAndBDCommunication;
import ru.spring.core.project.DBService.impl.UserStateServiceImpl;
import ru.spring.core.project.entity.UserState;
import ru.spring.core.project.weatherCommunication.RequesterDataFromDBOrOpenWeatherMap;
import ru.spring.core.project.DBService.impl.PlaceServiceImpl;
import ru.spring.core.project.DBService.impl.UserServiceImpl;
import ru.spring.core.project.DBService.impl.WeatherDataServiceImpl;
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
public class WeatherBot extends TelegramLongPollingBot {

    @Autowired
    private final BotConfig config;
    @Autowired
    private WeatherRequestHandler weatherRequestHandler;
    @Autowired
    private RequesterDataFromDBOrOpenWeatherMap requesterDataFromDBOrOpenWeatherMap;
    private static final Logger logger = LoggerFactory.getLogger(WeatherBot.class);
    private ClassPathXmlApplicationContext context;
    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private PlaceServiceImpl placeService;

    @Autowired
    private WeatherDataServiceImpl wheatherDataService;

    @Autowired
    private UserStateServiceImpl userStateService;
    @Autowired
    private ResponseWeatherForecastGenerator responseWeatherForecastGenerator;

    @Autowired
    private LRUCacheAndBDCommunication lruCacheAndBDCommunication;


    @Autowired
    // указываются сообщения в левом меню, главный конструктор бота
    public WeatherBot(ClassPathXmlApplicationContext context) {
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
        User currentUser;
        if(!lruCacheAndBDCommunication.userIsExist(chatId)){// первое сообщение от пользователя
            String userName = update.getMessage().getFrom().getFirstName();
            currentUser = new User(userName,chatId);
            UserState userState = new UserState(currentUser,BotState.BEFORE_START);
            currentUser.setUserState(userState);
            lruCacheAndBDCommunication.addUser(currentUser);
        }
        else{ // не первое
            currentUser = lruCacheAndBDCommunication.getUserByChatId(chatId);
        }

        logger.info("Обновление в состоянии : "+currentUser.getUserState().toString()+
                        " Пришло сообщение:" +update.getMessage().getText(),
                chatId );
        if(update.hasMessage() && update.getMessage().hasText()){
            handleMessege(update,currentUser);
        }
        else if (update.getMessage().hasLocation()) {
            handleLocation(update,currentUser);
        }
        else{
            errorAfterStartHandle(update,currentUser,2);
        }
    }
    //Реализация Конечного автомата
    private void handleMessege(Update update,User currentUser){
        String message = update.getMessage().getText();
        BotState currentState = currentUser.getUserState().getCurrentState();
        if(currentState==BotState.BEFORE_START){
            switch (message){
                case "/start":
                    startHandle(update,currentUser);
                    break;
                default:
                    errorBeforeStartHandle(update);
                    break;
            }
        }
        else if(currentState==BotState.AFTER_START){
            switch (message){
                case "/clear":
                    clearHandle(update,currentUser);
                    break;
                case "/show":
                    showHandle(update,currentUser);
                    break;
                case "/help":
                    helpHandle(update);
                    break;
                case "/start":
                    startHandle(update,currentUser);
                    break;
                default:
                    cityNameHandle(update,currentUser);
                    break;
            }
        }

        else if(currentState==BotState.SENT_CITY_NAME_OR_LOCATION){
            switch (message){
                case "/now":
                    forecastNowHandle(update,currentUser);
                    break;
                case "/today":
                    forecastNDayHandle(update,currentUser,0);
                    break;
                case "/2days":
                    forecastNDayHandle(update,currentUser,1);
                    break;
                case "/3days":
                    forecastNDayHandle(update,currentUser,2);
                    break;
                case "/5days":
                    forecastNDayHandle(update,currentUser,4);
                    break;
                default:
                    errorAfterStartHandle(update,currentUser,0);
                    break;
            }
        }
        else if(currentState==BotState.START_CLEARING){
            switch (message) {
                case "/yes":
                    clearYesHandle(update,currentUser);
                    break;
                case "/no":
                    clearNoHandle(update,currentUser);
                    break;
                default:
                    errorAfterStartHandle(update,currentUser,0);
                    break;

            }
        }
    }


    private void handleLocation(Update update,User currentUser){
        Location location = update.getMessage().getLocation();
        BotState currentState = currentUser.getUserState().getCurrentState();
        if(currentState==BotState.AFTER_START){
            correctReceiveLocationHandle(update,currentUser);
        }
        else{
            errorAfterStartHandle(update,currentUser,1);
        }
    }

    //Обработка полученных сообщений
    private void startHandle(Update update,User currentUser) {
        long chatId = update.getMessage().getChatId();
        String userName = update.getMessage().getFrom().getFirstName();
        String response = "Привет, " + userName + "!";
        sendMessage(chatId, response);
        sendInfoAfterStartMessege(chatId);
        currentUser.getUserState().setCurrentState(BotState.AFTER_START);
        lruCacheAndBDCommunication.updateUser(currentUser);
    }

    private void clearHandle(Update update,User currentUser){
        long chatId = update.getMessage().getChatId();
        String response ="Вы уверены, что хотите очистить все данные о вас?"+
                "\n/yes"+
                "\n/no";
        sendMessage(chatId, response);
        currentUser.getUserState().setCurrentState(BotState.START_CLEARING);
        lruCacheAndBDCommunication.updateUser(currentUser);
    }
    private void showHandle(Update update,User currentUser){
        long chatId = update.getMessage().getChatId();
        List<Place>listPlaces = currentUser.getListOfPlaces(); //placeService.getAllLinkedUserByChatId(chatId);
        String response="";
        for (Place p: listPlaces){
            if(!p.placeNameIsNull() && !p.getPlaceName().equals("")) {
                response += "/";
                response += p.getPlaceName();
                response += "\n";
            }
        }
        if(response.equals("")){
            response+="У вас пока нет ни одного сохраненного места.";
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

    private void cityNameHandle(Update update,User currentUser){
        long chatId = update.getMessage().getChatId();
        String messege = update.getMessage().getText();
        if(messege.charAt(0)=='/'){
            messege = messege.substring(1);
        }
        String response;
        try{
            Place currentPlace = requesterDataFromDBOrOpenWeatherMap.getPlaceIfExist(messege);
            currentUser.getUserState().setCurrentState(BotState.SENT_CITY_NAME_OR_LOCATION);
            currentUser.getUserState().setCurrentPlace(currentPlace);
            lruCacheAndBDCommunication.updateUser(currentUser);
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

    private void forecastNowHandle(Update update,User currentUser){
        long chatId = update.getMessage().getChatId();

        try {
            WeatherData weatherData = requesterDataFromDBOrOpenWeatherMap.
                    getWeatherDataByPlaceNow(currentUser.getUserState().getCurrentPlace());
            String response = responseWeatherForecastGenerator.getResponseBySingleWeatherData(weatherData);
            sendMessage(chatId,response);
        }
        catch (Exception e){
            String response ="Неизвестная ошибка на стороне сервера. Попробуйте заново.";
            sendMessage(chatId,response);
        }

        currentUser.getUserState().setCurrentState(BotState.AFTER_START);
        currentUser.addNewPlace(currentUser.getUserState().getCurrentPlace());
        lruCacheAndBDCommunication.updateUser(currentUser);
    }

    // эта функция отличается от предыдущей только вызовом NDaysForecast и ResponseByListOfWeatherData
    private void forecastNDayHandle(Update update,User currentUser, int amountDays){
        long chatId = update.getMessage().getChatId();


        try {
            List<WeatherData> listOfWeatherData = requesterDataFromDBOrOpenWeatherMap.
                    getWeatherDataByPlaceNDay(currentUser.getUserState().getCurrentPlace(),amountDays);
            List<String> listOfMessage = responseWeatherForecastGenerator.getResponseByListOfWeatherData(listOfWeatherData);
            for(String str: listOfMessage){
                sendMessage(chatId,str);
            }
        }
        catch (Exception e){
            String response ="Неизвестная ошибка на стороне сервера. Попробуйте заново.";
            sendMessage(chatId,response);
        }
        currentUser.addNewPlace(currentUser.getUserState().getCurrentPlace());
        currentUser.getUserState().setCurrentState(BotState.AFTER_START);
        lruCacheAndBDCommunication.updateUser(currentUser);
    }


    private void clearYesHandle(Update update, User currentUser){
        long chatId = update.getMessage().getChatId();

        UserState userState = currentUser.getUserState();
        currentUser.getUserState().setCurrentState(BotState.AFTER_START);
        lruCacheAndBDCommunication.updateUser(currentUser);



        String response = "";
        if(currentUser.getListOfPlaces()== null || currentUser.getListOfPlaces().isEmpty()){
            response = "У вас пока нет сохраненных мест.";
            sendMessage(chatId,response);
            return ;
        }
        currentUser = userService.deleteAllLinksWithPlaces(currentUser);
        lruCacheAndBDCommunication.updateUser(currentUser);
        response = "Ваша история поиска очищена.";
        sendMessage(chatId,response);
    }
    private void clearNoHandle(Update update, User currentUser){
        long chatId = update.getMessage().getChatId();

        currentUser.getUserState().setCurrentState(BotState.AFTER_START);
        lruCacheAndBDCommunication.updateUser(currentUser);

        String response = "Окей, не очищаем.";
        sendMessage(chatId,response);
    }

    private void correctReceiveLocationHandle(Update update, User currentUser){
        long chatId = update.getMessage().getChatId();

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

        Place currentPlace = new Place(coordinateForWeatherBot);
        currentUser.getUserState().setCurrentPlace(currentPlace);
        currentUser.getUserState().setCurrentState(BotState.SENT_CITY_NAME_OR_LOCATION);
        lruCacheAndBDCommunication.updateUser(currentUser);
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
    * option = 2 - полученно некорректное сообщение(например фото)
    * */
    private void errorAfterStartHandle(Update update, User currentUser, int option){
        long chatId = update.getMessage().getChatId();


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
                response = "Ошибка при обработке ошибок) Попробуйте заново.";
                break;
        }
        sendMessage(chatId, response);
        sendInfoAfterStartMessege(chatId);
        currentUser.getUserState().setCurrentState(BotState.AFTER_START);
        lruCacheAndBDCommunication.updateUser(currentUser);


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
