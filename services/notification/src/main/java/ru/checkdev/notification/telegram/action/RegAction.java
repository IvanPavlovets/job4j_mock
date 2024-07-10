package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.domain.TgUser;
import ru.checkdev.notification.service.TgUserService;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

import java.util.Calendar;
import java.util.Map;

/**
 * 3. Мидл
 * Класс реализует пункт меню регистрации нового пользователя в телеграм бот
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 12.09.2023
 */
@RequiredArgsConstructor
@Slf4j
public class RegAction implements Action {
    private final String sl = System.lineSeparator();
    private static final String ERROR_OBJECT = "error";
    private static final String URL_AUTH_REGISTRATION = "/registration";
    private final TgConfig tgConfig = new TgConfig("tg/", 8);
    private final TgAuthCallWebClint authCallWebClint;
    private final TgUserService tgUserService;
    private final String urlSiteAuth;

    private String username = "";

    private final String textName = "Введите имя для регистрации нового пользователя:";
    private String textEmail = "Введите email для регистрации:";

    @Override
    public BotApiMethod<Message> handle(Message message, Map<String, String> bindingBy) {
        var chatId = message.getChatId().toString();

        var tgUser = tgUserService.findByChatId(message.getChatId().intValue());
        if (tgUser != null) {
            textEmail = "Для Вашего аккаунта уже выполнена регистрация."
                    + sl
                    + "/start";
            return new SendMessage(chatId, textEmail);
        }

        var msg = message.getText();
        if (msg.equals("/new")) {
            return new SendMessage(chatId, textName);
        }
        username = message.getText();

        return new SendMessage(chatId, textEmail);
    }

    /**
     * Метод формирует ответ пользователю.
     * Весь метод разбит на 4 этапа проверки.
     * 1. Проверка на соответствие формату Email введенного текста.
     * 2. Отправка данных в сервис Auth и если сервис не доступен сообщаем
     * 3. Если сервис доступен, получаем от него ответ и обрабатываем его.
     * 3.1 ответ при ошибке регистрации
     * 3.2 ответ при успешной регистрации.
     *
     * @param message Message
     * @return BotApiMethod<Message>
     */
    @Override
    public BotApiMethod<Message> callback(Message message) {
        var chatId = message.getChatId().toString();
        var email = message.getText();
        var text = "";
        var sl = System.lineSeparator();

        if (!tgConfig.isEmail(email)) {
            text = "Email: " + email + " не корректный." + sl
                   + "попробуйте снова." + sl
                   + "/new";
            return new SendMessage(chatId, text);
        }

        var password = tgConfig.getPassword();
        var person = new PersonDTO(username, email, password, true, null,
                Calendar.getInstance());
        Object result;
        try {
            result = authCallWebClint.doPost(URL_AUTH_REGISTRATION, person).block();
        } catch (Exception e) {
            log.error("WebClient doPost error: {}", e.getMessage());
            text = "Сервис не доступен попробуйте позже" + sl
                   + "/start";
            return new SendMessage(chatId, text);
        }

        Map<String, Map> mapObject = tgConfig.getObjectToMap(result);

        if (mapObject.containsKey(ERROR_OBJECT)) {
            text = "Ошибка регистрации: " + mapObject.get(ERROR_OBJECT);
            return new SendMessage(chatId, text);
        }

        int userId = (int) mapObject.get("person").get("id");

        tgUserService.save(new TgUser(0, userId, Integer.parseInt(chatId)));

        text = "Вы зарегистрированы: " + sl
               + "Имя: " + username + sl
               + "Логин: " + email + sl
               + "Пароль: " + password + sl
               + urlSiteAuth;
        return new SendMessage(chatId, text);
    }

}
