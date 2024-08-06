package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.TgUser;
import ru.checkdev.notification.service.TgUserService;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClient;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CheckActionTest {

    private CheckAction checkAction;
    private TgUserService tgUserService;
    private TgAuthCallWebClient tgAuthCallWebClient;
    private String urlSitePerson = "";
    private Message messageMock;
    private Map<String, String> bindingBy = new HashMap<>();
    private String sl = System.lineSeparator();

    @BeforeEach
    void init() {
        tgUserService = mock(TgUserService.class);
        tgAuthCallWebClient = mock((TgAuthCallWebClient.class));
        messageMock = mock(Message.class);
        checkAction = new CheckAction(tgUserService, tgAuthCallWebClient, urlSitePerson);
    }

    @Test
    public void whenNotRegisteredThenReturnDemandToRegister() {
        when(tgUserService.findByChatId(anyInt())).thenReturn(null);
        String expected = "Для Вашего аккаунта регистрация не выполнена."
                + sl
                + "/start";
        BotApiMethod<Message> res = checkAction.handle(messageMock, bindingBy);
        assertThat(res.toString()).contains(expected);
    }

    @Test
    public void whenAuthServiceIsUnavailableThenReturnDemandToWait() {
        when(tgUserService.findByChatId(anyInt())).thenReturn(new TgUser());
        when(tgAuthCallWebClient.doGetPerson(anyString())).thenThrow(RuntimeException.class);
        String expected = "Сервис не доступен попробуйте позже" + sl
                + "/start";
        BotApiMethod<Message> res = checkAction.handle(messageMock, bindingBy);
        assertThat(res.toString()).contains(expected);
    }

    @Test
    public void whenSuccessRequestThenReturnNameAndEmail() {
        Map<String, Map<String, String>> outerMap = new HashMap<>();
        Map<String, String> innerMap = new HashMap<>();
        innerMap.put("username", "Ivan");
        innerMap.put("email", "ivan@email.com");
        outerMap.put("person", innerMap);
        when(tgUserService.findByChatId(anyInt())).thenReturn(new TgUser());
        when(tgAuthCallWebClient.doGetPerson(anyString())).thenReturn(Mono.just(outerMap));

        String expected = "ФИО: Ivan"
                + sl
                + "Email: ivan@email.com"
                + sl;
        BotApiMethod<Message> res = checkAction.handle(messageMock, bindingBy);
        assertThat(res.toString()).contains(expected);
    }

}
