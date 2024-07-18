package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.service.TgUserService;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

import java.util.Calendar;
import java.util.Map;

@AllArgsConstructor
@Slf4j
public class ForgetAction implements Action {
    private final String sl = System.lineSeparator();
    private final TgConfig tgConfig = new TgConfig("tg/", 8);
    private final TgUserService tgUserService;
    private final TgAuthCallWebClint authCallWebClint;
    private final String urlSiteChangePassword;

    @Override
    public BotApiMethod<Message> handle(Message message, Map<String, String> bindingBy) {
        var chatId = message.getChatId().toString();
        var text = "";
        var tgUser = tgUserService.findByChatId(message.getChatId().intValue());
        if (tgUser == null) {
            text = String.format("Для Вашего аккаунта регистрация не выполнена. %s/start", sl);
            return send(chatId, text, bindingBy);
        }

        var password = tgConfig.getPassword();
        var person = new PersonDTO("", "", password, true, null, null);
        var urlPerson = urlSiteChangePassword + tgUser.getUserId();
        try {
            authCallWebClint.doPost(urlPerson, person).block();
        } catch (Exception e) {
            log.error("WebClient doPost error: {}", e.getMessage());
            text = String.format("Сервис не доступен попробуйте позже %s/start", sl);
            return send(chatId, text, bindingBy);
        }

        return send(chatId, "Новый пароль: " + password, bindingBy);
    }

    @Override
    public BotApiMethod<Message> callback(Message message, Map<String, String> bindingBy) {
        return null;
    }

    private BotApiMethod<Message> send(String chatId, String out, Map<String, String> bindingBy) {
        bindingBy.remove(chatId);
        return new SendMessage(chatId, out);
    }
}
