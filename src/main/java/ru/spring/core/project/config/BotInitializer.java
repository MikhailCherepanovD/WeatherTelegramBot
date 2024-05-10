package ru.spring.core.project.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.spring.core.project.service.Bot;

@Component
@Slf4j
public class BotInitializer {
    private final BotConfig botConfig;

    @Autowired
    public BotInitializer(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException {
        Bot bot = new Bot(botConfig);
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            telegramBotsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            log.error("Error occurred : " + e.getMessage()); //
        }
    }
}
