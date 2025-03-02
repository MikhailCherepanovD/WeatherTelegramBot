package ru.spring.core.project;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.spring.core.project.config.BotInitializer;

public class Main {
    public static void main(String[] args) throws TelegramApiException {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/applicationContext.xml");
        //context.getBean(BotInitializer.class).init();
    }
}