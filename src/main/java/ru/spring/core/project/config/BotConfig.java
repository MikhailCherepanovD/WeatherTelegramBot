package ru.spring.core.project.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"ru.spring.core.project"})
public class BotConfig {
    @Value("${botConfig.token}")
     private String botToken;
    @Value("${botConfig.name}")
     private String botName;
    @Value("${botConfig.openWeatherMapKey")
    String openWeatherMapKey;

    public String getOpenWeatherMapKey() {
        return openWeatherMapKey;
    }

    public String getBotToken() {
        return botToken;
    }

    public String getBotName() {
        return botName;
    }
}
