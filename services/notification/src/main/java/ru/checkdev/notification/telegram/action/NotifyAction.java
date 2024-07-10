package ru.checkdev.notification.telegram.action;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Map;

public class NotifyAction implements Action {
    @Override
    public BotApiMethod<Message> handle(Message message, Map<String, String> bindingBy) {
        return null;
    }

    @Override
    public BotApiMethod<Message> callback(Message message) {
        return null;
    }
}
