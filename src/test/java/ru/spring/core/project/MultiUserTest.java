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

public class MultiUserTest {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/applicationContext.xml");
    Bot bot = context.getBean(Bot.class);

    PlaceServiceImpl placeService = context.getBean(PlaceServiceImpl.class);
    String[] cities = {
            "Москва", "Санкт-Петербург", "Кемерово", "Екатеринбург", "Якутск", "Казань", "Челябинск",
            "Омск", "Самара", "Ростов-на-Дону", "Уфа", "Красноярск", "Воронеж", "Пермь", "Волгоград", "Краснодар", "Саратов","Тюмень","Тольятти", "Курган"
    };

    @Test
    public void telegrammCommunicateTest() throws Exception {
        File myFile= new File("src/test/java/ru/spring/core/project/TestLog/MultiUserTest.log");
        myFile.getParentFile().mkdirs();
        FileOutputStream outputStream = new FileOutputStream(myFile);
        myFile.createNewFile();

        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        User user = new User();

        for(Long i=0L;i<10L;i++){
            user.setFirstName("User"+i.toString());
            chat.setId(i);
            message.setChat(chat);
            message.setFrom(user);
            message.setText("/start");
            update.setMessage(message);
            bot.onUpdateReceived(update);
        }
        for(int k=0;k<10;k++){
            for(Long i =0L;i<10L;i++){
                user.setFirstName("User"+i.toString());
                chat.setId(i);
                message.setChat(chat);
                message.setFrom(user);
                message.setText(cities[k+(int)(long)i]);
                update.setMessage(message);
                bot.onUpdateReceived(update);

                message.setText("/now");
                update.setMessage(message);
                if(k==2) {
                    int h=10;
                    h++;
                }
                    bot.onUpdateReceived(update);

            }
        }



        byte[] buffer = ("До /clear:\n").getBytes();
        outputStream.write(buffer);
        for(long id = 0L;id<10L;id++) {
            buffer = ("\nUser id: "+id+ "\n").getBytes();
            outputStream.write(buffer);
            List<Place> places = placeService.getAllLinkedUserByChatId(id);
            for (Place p : places) {
                buffer = (p.getPlaceName() + "\n").getBytes();
                outputStream.write(buffer);
            }
        }



        for(Long i=0L;i<10L;i++){
            user.setFirstName("User"+i.toString());
            chat.setId(i);
            message.setChat(chat);
            message.setFrom(user);
            message.setText("/clear");
            update.setMessage(message);
            bot.onUpdateReceived(update);
        }

        for(Long i=0L;i<10L;i++){
            user.setFirstName("User"+i.toString());
            chat.setId(i);
            message.setChat(chat);
            message.setFrom(user);
            message.setText("/yes");
            update.setMessage(message);
            bot.onUpdateReceived(update);
        }


        buffer = ("\nПосле /clear:\n").getBytes();
        outputStream.write(buffer);

        for(long id = 0L;id<10L;id++) {
            buffer = ("\nUser id: "+id+ "\n").getBytes();
            outputStream.write(buffer);
            List<Place> places = placeService.getAllLinkedUserByChatId(id);
            for (Place p : places) {
                buffer = (p.getPlaceName() + "\n").getBytes();
                outputStream.write(buffer);
            }
        }



    }

}