package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

public class InfoActionTest {
    private List<String> actions = List.of("/start - Доступные команды",
            "/new - Зарегистрировать нового пользователя",
            "/echo - Команды echo",
            "/check - Выдать ФИО и почту, привязанную к этому аккаунту",
            "/forget - Восстановить пароль");
    

    private InfoAction infoAction = new InfoAction(actions);
    private Message messageMock = mock(Message.class);
    private String sl = System.lineSeparator();

    @Test
    public void whenGetInfoActionThenReturnMenu() {
        BotApiMethod<Message> actual = infoAction.handle(messageMock, new HashMap<>());
        var out = new StringBuilder();
        out.append("Выберите действие:").append(sl);
        for (String action : actions) {
            out.append(action).append(sl);
        }
        String expected = out.toString();
        assertThat(actual.toString()).contains(expected);
    }

}
