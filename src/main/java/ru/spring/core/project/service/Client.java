package ru.spring.core.project.service;

import java.util.ArrayList;
import java.util.List;

public class Client {
    private String name;
    private String chatId;
    private String baseCity;
    private List<String> listOfCitys;

    public Client(String name, String chatId, String baseCity, List<String> listOfCitys) {
        this.name = name;
        this.chatId = chatId;
        this.baseCity = baseCity;
        this.listOfCitys = new ArrayList<>();
    }
}
