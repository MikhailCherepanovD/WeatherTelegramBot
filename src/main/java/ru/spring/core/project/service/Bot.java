package ru.spring.core.project.service;

import lombok.extern.slf4j.Slf4j;
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
import ru.spring.core.project.config.BotConfig;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class Bot extends TelegramLongPollingBot {

   private final BotConfig config;
   static final String HELP_MESSAGE = "This bot идет нахуй потому что тут писать еще нечего";

   public Bot(BotConfig config) {
       this.config = config;
       List<BotCommand> listOfCommands = new ArrayList();
       listOfCommands.add(new BotCommand("/start", "get a welcome message"));
       listOfCommands.add(new BotCommand("/sign_up", "sign up"));
       listOfCommands.add(new BotCommand("/sign_in", "sign in"));
       listOfCommands.add(new BotCommand("/location", "request location"));
       listOfCommands.add(new BotCommand("/help", "show help message"));

       try {
           this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
       } catch (TelegramApiException e){
           log.error("Error setting bot commands", e.getMessage());
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

    public String getOpenWeatherMapKey(){
       return config.getOpenWeatherMapKey();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (message){
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/help":
                    sendMessage(chatId, HELP_MESSAGE);
                    break;
                case "/location":
                     requestLocation(chatId);
                     break;
                default:
                    sendMessage(chatId, "Sorry, command was not recognized");
            }
        } else if(update.hasMessage() && update.getMessage().hasLocation()){
            Location location = update.getMessage().getLocation();
            handleLocation(location, update.getMessage().getChatId());
        }
    }

    private void handleLocation(Location location, Long chatId) {
       sendMessage(chatId, "Thank you for using the bot. We will start finding your city.");
       Double latitude = location.getLatitude();
       Double longitude = location.getLongitude();
       SendMessage message = new SendMessage();
       message.setChatId(chatId);
       message.setText("Additional coordinates:\nLatitude - " + latitude + ", Longitude - " + longitude);

       try {
           execute(message);
       } catch (TelegramApiException e) {
           log.error("Error sending location", e);
       }
    }

    private void startCommandReceived (long chatId, String name) {
        String response = "Hello " + name + ". I provide an opportunity to find out the weather anywhere in the world at any time.";
        log.info("Replied to user. response: " + response + " chatId: " + chatId + " name: " + name);
        sendMessage(chatId, response);
    }

    private void sendMessage(long chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(message);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Error sending message", e);
        }
    }

    private void requestLocation(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Please share your location with me.");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton();
        button.setText("Send Location");
        button.setRequestLocation(true);
        row.add(button);
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Error requesting location", e);
        }

    }

}
