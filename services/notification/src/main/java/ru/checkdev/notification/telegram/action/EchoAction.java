package ru.checkdev.notification.telegram.action;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.HashMap;
import java.util.Map;

public class EchoAction implements Action {
    private final String action;
    private String username = "";

    private final String sl = System.lineSeparator();
    private final String textName = "Введите имя для регистрации нового пользователя:";
    private final String textEmail = "Введите email для регистрации:";

    public EchoAction(String action) {
        this.action = action;
    }

    /**
     * Дествие handle будет выполнено когда пользователь выбирет пункт меню "echo"
     * после этого запишем данные в карту (bindingBy) и пользователь вводит
     * какието данные, наживает отправляемт события, карта видит что есть
     * связоное событие (предыдущее действие пользователя) выбрал меню и
     * обрабатываеться событие в callback
     * @param message
     * @return
     */
    @Override
    public BotApiMethod<Message> handle(Message message, Map<String, String> bindingBy) {
        var msg = message.getText();
        var chatId = message.getChatId().toString();
        if (action.equals(msg)) {
            return new SendMessage(chatId, textName);
        }
        username = message.getText();
        return new SendMessage(chatId, textEmail);
    }

    /**
     * Получаем, какое событие мы выбрали до этого и
     * что нужно с этим сделать
     * @param message
     * @return
     */
    @Override
    public BotApiMethod<Message> callback(Message message, Map<String, String> bindingBy) {
        var chatId = message.getChatId().toString();
        var out = new StringBuilder();

        out.append("username: ").append(username).append(sl);
        out.append("email: ").append(message.getText()).append(sl);

        return new SendMessage(chatId, out.toString());
    }

}
