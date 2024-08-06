package ru.checkdev.notification.telegram;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.checkdev.notification.telegram.action.Action;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 3. Мидл
 * Реализация меню телеграм бота.
 * TelegramLongPollingBot, создает отдельную нить и начинает
 * слушать события, которые приходят в onUpdateReceived(Update update)
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 12.09.2023
 */
public class BotMenu extends TelegramLongPollingBot {
    private final Map<String, String> bindingBy = new ConcurrentHashMap<>();
    private final Map<String, Action> actions;
    private final String username;
    private final String token;


    public BotMenu(Map<String, Action> actions, String username, String token) throws TelegramApiException {
        this.actions = actions;
        this.username = username;
        this.token = token;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    /**
     * 1) В методе проверяем что update новое событие, если да,
     * то получаем key - какое это событие.
     * получаем chatId - ботом может пользоваться кто угодно.
     * 2) if (actions.containsKey(key)) - если такой ключ есть в поле action,
     * то берем обьект (actions.get(key).) и отуда получаем сообщения msg,
     * которое можно отправить send(msg) в чат, пользователю.
     * 3) bindingBy.put(chatId, key) - карта связывает чат и выбраное действие меню,
     * это меню необходимо, когда пользователь будет вводить данные.
     * 4)  else if (!(key.contains("@")) && bindingBy.containsValue("/new") ), при регистрации
     * спрашиваем 2 раза, имя и почту.
     * 5) else if (bindingBy.containsKey(chatId)) - случай если не нашли никакого action,
     * пользователь ввел просто текст. Ищем какое действие было до этого и у этого действия
     * вызываем обработчик callback(update.getMessage() и удаляем это действие из связных событий,
     * что бы не было повторов, обрабочик уже произашел, bindingBy.remove(chatId).
     * @param update - содежиться либо новое сообщение или событие по редактированию,
     *               изменению состояния чата.
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            var key = update.getMessage().getText();
            var chatId = update.getMessage().getChatId().toString();
            if (actions.containsKey(key)) {
                bindingBy.put(chatId, key);
                var msg = actions.get(key).handle(update.getMessage(), bindingBy);
                send(msg);
            } else if (!(key.contains("@")) && bindingBy.containsValue("/new")) {
                var msg = actions.get("/new").handle(update.getMessage(), bindingBy);
                send(msg);
            } else if (bindingBy.containsKey(chatId)) {
                Action action = actions.get((bindingBy.get(chatId)));
                bindingBy.remove(chatId);
                var msg = action.callback(update.getMessage(), bindingBy);
                send(msg);
            } else {
                send(new SendMessage(chatId, "введена не существующая команда"));
            }
        }
    }

    /**
     * метод передает сообщения в чат пользователю.
     * @param msg
     */
    private void send(BotApiMethod msg) {
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
