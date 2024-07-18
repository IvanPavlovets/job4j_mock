package ru.checkdev.notification.telegram.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.service.TgUserService;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;
import ru.checkdev.notification.telegram.service.TgMockCallWebClint;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Slf4j
public class SubscribeAction implements Action {
    private final Map<String, String> idEmail = new ConcurrentHashMap<>();
    private static final String URL_USER_CHECK = "/person/check";
    private static final String URL_USER_SUBSCRIBE = "/subscribe";
    private final String textEmail = "Введите email:";
    private final String textPassword = "Введите password:";
    private final String sl = System.lineSeparator();
    private String action;
    private final TgUserService tgUserService;
    private final TgAuthCallWebClint tgAuthCallWebClint;
    private final TgMockCallWebClint tgMockCallWebClint;

    @Override
    public BotApiMethod<Message> handle(Message message, Map<String, String> bindingBy) {
        var chatId = message.getChatId().toString();
        var text = "";
        action = bindingBy.get(chatId);
        var tgUser = tgUserService.findByChatId(message.getChatId().intValue());
        if (tgUser == null) {
            text = String.format("Для Вашего аккаунта регистрация не выполнена. %s/start", sl);
            return new SendMessage(chatId, text);
        }

        if (!idEmail.containsKey(chatId)) {
            return new SendMessage(chatId, textEmail);
        }
        return new SendMessage(chatId, textPassword);
    }

    @Override
    public BotApiMethod<Message> callback(Message message, Map<String, String> bindingBy) {
        var chatId = message.getChatId().toString();
        if (!idEmail.containsKey(chatId)) {
            idEmail.put(chatId, message.getText());
            bindingBy.put(chatId, action);
            return handle(message, bindingBy);
        }

        PersonDTO personDTO = new PersonDTO("",
                idEmail.get(chatId), message.getText(), true, null, null);
        bindingBy.remove(chatId);
        idEmail.remove(chatId);
        boolean resAuth;
        try {
            resAuth = (boolean) tgAuthCallWebClint.doPost(URL_USER_CHECK, personDTO).block();
            if (!resAuth) {
                String text = String.format("Введены неверные данные %s/start", sl);
                return send(chatId, text, bindingBy);
            }
        } catch (Exception e) {
            log.error("WebClient doPost error: {}", e.getMessage());
            String text = String.format("Сервис не доступен попробуйте позже %s/start", sl);
            return send(chatId, text, bindingBy);
        }

        try {
            var res = tgMockCallWebClint.doPost(URL_USER_SUBSCRIBE, message.getChatId()).block();
            if (res == null) {
                String text = String.format("Вы ранее уже были подписаны %s/start", sl);
                return send(chatId, text, bindingBy);
            }
        } catch (Exception e) {
            log.error("WebClient doPost error: {}", e.getMessage());
            String text = String.format("Сервис не доступен попробуйте позже %s/start", sl);
            return send(chatId, text, bindingBy);
        }
        return send(chatId, "Вы подписаны", bindingBy);
    }

    private BotApiMethod<Message> send(String chatId, String out, Map<String, String> bindingBy) {
        bindingBy.remove(chatId);
        return new SendMessage(chatId, out);
    }

}
