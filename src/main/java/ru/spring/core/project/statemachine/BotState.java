package ru.spring.core.project.statemachine;

public enum BotState {
    BEFORE_START,
    AFTER_START,
    SENT_CITY_NAME_OR_LOCATION,
    START_CLEARING
}