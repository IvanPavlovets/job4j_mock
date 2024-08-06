package ru.checkdev.notification.telegram.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.exception.ConstraintKeyException;
import ru.checkdev.notification.service.TgUserService;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClient;
import ru.checkdev.notification.telegram.service.TgMockCallWebClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс реализует привязку пользователя с проверкой почты и пароля
 */

@RequiredArgsConstructor
@Slf4j
public class SubscribeAction implements Action {
    private final Map<String, String> idEmail = new ConcurrentHashMap<>();
    private final String sl = System.lineSeparator();
    private String action;
    private final TgUserService tgUserService;
    private final TgAuthCallWebClient tgAuthCallWebClient;
    private final TgMockCallWebClient tgMockCallWebClient;
    private final String urlUserCheck;
    private final String urlUserSubcribe;

    @Override
    public BotApiMethod<Message> handle(Message message, Map<String, String> bindingBy) {
        var chatId = message.getChatId().toString();
        var text = "";
        action = bindingBy.get(chatId);
        var tgUser = tgUserService.findByChatId(message.getChatId().intValue());
        if (tgUser == null) {
            text = String.format("Для Вашего аккаунта регистрация не выполнена.%s/start", sl);
            return new SendMessage(chatId, text);
        }

        if (!idEmail.containsKey(chatId)) {
            String textEmail = "Введите email:";
            return new SendMessage(chatId, textEmail);
        }
        String textPassword = "Введите password:";
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
            resAuth = (boolean) tgAuthCallWebClient.doPost(urlUserCheck, personDTO).block();
            if (!resAuth) {
                String text = String.format("Введены неверные данные %s/start", sl);
                return send(chatId, text, bindingBy);
            }
        } catch (Exception e) {
            log.error("WebClient doPost error: {}", e.getMessage());
            String text = String.format("Сервис недоступен попробуйте позже %s/start", sl);
            return send(chatId, text, bindingBy);
        }

        try {
            tgMockCallWebClient.doPost(urlUserSubcribe, message.getChatId()).block();
        } catch (ConstraintKeyException e) {
            log.error("WebClient doGet error: {}", e.getMessage());
            String text = String.format("Вы ранее уже были подписаны %s/start", sl);
            return send(chatId, text, bindingBy);
        } catch (Exception e) {
            log.error("WebClient doPost error: {}", e.getMessage());
            String text = String.format("Сервис недоступен попробуйте позже %s/start", sl);
            return send(chatId, text, bindingBy);
        }
        return send(chatId, "Вы подписаны", bindingBy);
    }

    private BotApiMethod<Message> send(String chatId, String out, Map<String, String> bindingBy) {
        bindingBy.remove(chatId);
        return new SendMessage(chatId, out);
    }

}
