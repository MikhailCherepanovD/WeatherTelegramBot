package ru.spring.core.project;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.spring.core.project.DBService.impl.PlaceServiceImpl;
import ru.spring.core.project.entity.Place;
import ru.spring.core.project.service.Bot;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class SingleUserTest {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/applicationContext.xml");
    Bot bot = context.getBean(Bot.class);

    PlaceServiceImpl placeService=context.getBean(PlaceServiceImpl.class);
    @Test
    public void telegrammCommunicateTest() throws Exception{



        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        User user = new User();

        user.setFirstName("User1");
        chat.setId(1L);
        message.setChat(chat);
        message.setFrom(user);
        message.setText("/start");
        update.setMessage(message);
        bot.onUpdateReceived(update);

        message.setText("Москва");
        update.setMessage(message);
        bot.onUpdateReceived(update);

        message.setText("/2days");
        update.setMessage(message);
        bot.onUpdateReceived(update);


        message.setText("Санкт-Петербург");
        update.setMessage(message);
        bot.onUpdateReceived(update);
        message.setText("/2days");
        update.setMessage(message);
        bot.onUpdateReceived(update);

        message.setText("Абу-Даби");
        update.setMessage(message);
        bot.onUpdateReceived(update);
        message.setText("/2days");
        update.setMessage(message);
        bot.onUpdateReceived(update);

        message.setText("Челябинск");
        update.setMessage(message);
        bot.onUpdateReceived(update);
        message.setText("/2days");
        update.setMessage(message);
        bot.onUpdateReceived(update);

        message.setText("Бийск");
        update.setMessage(message);
        bot.onUpdateReceived(update);
        message.setText("/2days");
        update.setMessage(message);
        bot.onUpdateReceived(update);

        File myFile= new File("src/test/java/ru/spring/core/project/TestLog/SingleUserTest.log");
        myFile.getParentFile().mkdirs();
        FileOutputStream outputStream = new FileOutputStream(myFile);
        if (myFile.createNewFile()) {
            System.out.println("File created: " + myFile.getName());
        } else {
            System.out.println("File already exists.");
        }
        byte[] buffer = ("До /clear:\n").getBytes();
        outputStream.write(buffer);
        List<ru.spring.core.project.entity.Place> places = placeService.getAllLinkedUserByChatId(1L);
        for(Place p:places) {
             buffer = (p.getPlaceName() + "\n").getBytes();
            outputStream.write(buffer);
        }
        buffer = ("\nПосле /clear:\n").getBytes();
        outputStream.write(buffer);


        message.setText("/clear");
        update.setMessage(message);
        bot.onUpdateReceived(update);
        message.setText("/yes");
        update.setMessage(message);
        bot.onUpdateReceived(update);

        places = placeService.getAllLinkedUserByChatId(1L);
        for(Place p:places) {
            buffer = (p.getPlaceName() + "\n").getBytes();
            outputStream.write(buffer);
        }
    }
}
