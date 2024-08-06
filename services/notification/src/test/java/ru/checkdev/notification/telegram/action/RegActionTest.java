package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.domain.TgUser;
import ru.checkdev.notification.service.TgUserService;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClient;

import java.util.HashMap;
import java.util.Map;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class RegActionTest {
    private RegAction regAction;

    private TgUserService tgUserService;
    private TgAuthCallWebClient tgAuthCallWebClient;

    private Message message;

    private Map<String, String> bindingBy = new HashMap<>();
    private String sl = System.lineSeparator();
    private String urlSiteAuth = "urlSiteAuth";

    @BeforeEach
    void init() {
        tgUserService = mock(TgUserService.class);
        tgAuthCallWebClient = mock((TgAuthCallWebClient.class));
        message = mock(Message.class);
        regAction = new RegAction(tgAuthCallWebClient, tgUserService, urlSiteAuth);
    }

    @Test
    public void whenUserAlreadyHaveRegisteredThenReturnRejection() {
        when(tgUserService.findByChatId(anyInt())).thenReturn(new TgUser());
        String expected = "Для Вашего аккаунта уже выполнена регистрация."
                + sl
                + "/start";
        BotApiMethod<Message> res = regAction.handle(message, bindingBy);
        assertThat(res.toString()).contains(expected);
    }

    @Test
    public void whenNoUserRegisteredThenReturnMessageToEnterName() {
        when(tgUserService.findByChatId(anyInt())).thenReturn(null);
        when(message.getText()).thenReturn("/new");
        String expected = "Введите имя для регистрации нового пользователя:";
        BotApiMethod<Message> res = regAction.handle(message, bindingBy);
        assertThat(res.toString()).contains(expected);
    }

    @Test
    public void whenNoUserRegisteredThenReturnMessageToEnterEmail() {
        when(tgUserService.findByChatId(anyInt())).thenReturn(null);
        when(message.getText()).thenReturn("Ivan");
        String expected = "Введите email для регистрации:";
        BotApiMethod<Message> res = regAction.handle(message, bindingBy);
        assertThat(res.toString()).contains(expected);
    }

    @Test
    public void whenEnteredNotCorrectEmailThenReturnAlarmMessage() {
        String email = "ivangmail.com";

        when(message.getText()).thenReturn(email);

        String expected = String.format("Email: %s не корректный.%sпопробуйте снова", email, sl);
        BotApiMethod<Message> res = regAction.callback(message, bindingBy);
        assertThat(res.toString()).contains(expected);
    }

    @Test
    public void whenEnteredCorrectEmailButServerNotAvailableThenReturnAlarmMessage() {
        String email = "ivan@gmail.com";

        when(message.getText()).thenReturn(email);
        when(tgAuthCallWebClient.doPost(anyString(), any(PersonDTO.class))).thenThrow(new RuntimeException("RegActionTest"));

        String expected = String.format("Сервис не доступен попробуйте позже%s/start", sl);
        BotApiMethod<Message> res = regAction.callback(message, bindingBy);
        assertThat(res.toString()).contains(expected);
    }

    @Test
    public void whenServerReturnedErrorObjectThenReturnErrorMessage() {
        String email = "ivan@gmail.com";
        HashMap<String, HashMap<String, String>> ret = new HashMap<>();
        ret.put("error", new HashMap<>());

        when(message.getText()).thenReturn(email);
        when(tgAuthCallWebClient.doPost(anyString(), any(PersonDTO.class))).thenReturn(Mono.just(ret));

        String expected = String.format("Ошибка регистрации:");
        BotApiMethod<Message> res = regAction.callback(message, bindingBy);
        assertThat(res.toString()).contains(expected);
    }

    @Test
    public void whenSuccessfulRegisterUserThenReturnRegistrationMessage() {
        String name = "";
        String email = "ivan@gmail.com";
        HashMap<String, Object> innerMap = new HashMap<>();
        innerMap.put("id", 1);
        HashMap<String, Map> outerMap = new HashMap<>();
        outerMap.put("person", innerMap);

        when(message.getText()).thenReturn(email);
        when(message.getText()).thenReturn(email);
        when(tgAuthCallWebClient.doPost(anyString(), any(PersonDTO.class))).thenReturn(Mono.just(outerMap));

        String expected = String.format("Вы зарегистрированы: %sИмя: %s%sЛогин: %s", sl, name, sl, email);
        BotApiMethod<Message> res = regAction.callback(message, bindingBy);
        assertThat(res.toString()).contains(expected);
    }


}
