package ru.spring.core.project.Bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Bot extends TelegramLongPollingBot {
    final private String BOT_TOKEN = "7113783266:AAF9oZF2olFFLsV9KD6_JQFCiQzuRXT3gYc";
    final private String BOT_NAME = "killBonJoviWeatherBot";

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                Message message = update.getMessage();
                String chatId = message.getChatId().toString();
                String response = parseMessage(message.getText());
                SendMessage sendMessage = new SendMessage();

                sendMessage.setChatId(chatId);
                sendMessage.setText(response);
                execute(sendMessage);
            }
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private String parseMessage(String message) {
        String response;

        if(message.equals("/start")){
            response = "Здарова заебал чо погодку узнать хочешь?";
        }else{
            response = "Иди нахуй заебал что так не понятно???";
        }

        return response;
    }
}
