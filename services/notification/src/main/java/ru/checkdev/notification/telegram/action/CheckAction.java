package ru.checkdev.notification.telegram.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.service.TgUserService;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClient;

import java.util.Map;

/**
 * /check - выдать ФИО и почту, привязанную к этому аккаунту
 */
@RequiredArgsConstructor
@Slf4j
public class CheckAction implements Action {

    private final TgUserService tgUserService;
    private final TgAuthCallWebClient authCallWebClint;
    private final String urlSitePerson;

    @Override
    public BotApiMethod<Message> handle(Message message, Map<String, String> bindingBy) {
        var sl = System.lineSeparator();
        var chatId = message.getChatId().toString();
        var text = "";
        var tgUser = tgUserService.findByChatId(message.getChatId().intValue());
        if (tgUser == null) {
            text = String.format("Для Вашего аккаунта регистрация не выполнена.%s/start", sl);
            return send(chatId, text, bindingBy);
        }

        Map<String, Map> personDTO;
        var urlPerson = urlSitePerson + tgUser.getUserId();
        try {
            personDTO = authCallWebClint.doGetPerson(urlPerson).block();
        } catch (Exception e) {
            log.error("WebClient doGetPerson error: {}", e.getMessage());
            text = String.format("Сервис не доступен попробуйте позже%s/start", sl);
            return send(chatId, text, bindingBy);
        }

        var person = (Map) personDTO.get("person");

        var out = new StringBuilder();

        out.append("ФИО: ").append(person.get("username")).append(sl);
        out.append("Email: ").append(person.get("email")).append(sl);
        return send(chatId, out.toString(), bindingBy);
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
