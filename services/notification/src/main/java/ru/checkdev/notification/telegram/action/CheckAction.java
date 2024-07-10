package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.service.TgUserService;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class CheckAction implements Action {

    private final TgUserService tgUserService;

//    private static final String URL_AUTH_CHECK = "/person";
//
//    private final TgConfig tgConfig = new TgConfig("tg/", 8);
//
    private final TgAuthCallWebClint authCallWebClint;
    private final String urlSitePerson;

    @Override
    public BotApiMethod<Message> handle(Message message, Map<String, String> bindingBy) {
        var sl = System.lineSeparator();
        var chatId = message.getChatId().toString();
        var tgUser = tgUserService.findByChatId(message.getChatId().intValue());
        var text = "";
        if (tgUser == null) {
            text = "Для Вашего аккаунта регистрация не выполнена."
                    + sl
                    + "/start";
            return send(chatId, text, bindingBy);
        }

        Map<String, Map> personDTO;
        var urlPerson = urlSitePerson + tgUser.getUserId();
        try {
            personDTO = authCallWebClint.doGetPerson(urlPerson).block();
        } catch (Exception e) {
            log.error("WebClient doGetPerson error: {}", e.getMessage());
            text = "Сервис не доступен попробуйте позже" + sl
                    + "/start";
            return send(chatId, text, bindingBy);
        }

        var person = (Map) personDTO.get("person");

        var out = new StringBuilder();

        out.append("ФИО: ").append(person.get("username")).append(sl);
        out.append("Email: ").append(person.get("email")).append(sl);
        return send(chatId, out.toString(), bindingBy);
    }

    @Override
    public BotApiMethod<Message> callback(Message message) {
        return null;
    }

    private BotApiMethod<Message> send(String chatId, String out, Map<String, String> bindingBy) {
        bindingBy.remove(chatId);
        return new SendMessage(chatId, out);
    }

}
