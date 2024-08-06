package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.domain.TgUser;
import ru.checkdev.notification.exception.ConstraintKeyException;
import ru.checkdev.notification.service.TgUserService;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClient;
import ru.checkdev.notification.telegram.service.TgMockCallWebClient;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

public class SubscribeActionTest {
    private SubscribeAction subscribeAction;

    private TgUserService tgUserService;
    private TgAuthCallWebClient tgAuthCallWebClient;
    private TgMockCallWebClient tgMockCallWebClient;

    private Message message;

    private Map<String, String> bindingBy = new HashMap<>();
    private String sl = System.lineSeparator();

    private final String urlUserCheck = "urlUserCheck";
    private final String urlUserSubscribe = "urlUserSubscribe";

    @BeforeEach
    void init() {
        message = mock(Message.class);
        tgUserService = mock(TgUserService.class);
        tgAuthCallWebClient = mock((TgAuthCallWebClient.class));
        tgMockCallWebClient = mock((TgMockCallWebClient.class));
        subscribeAction = new SubscribeAction(
                tgUserService,
                tgAuthCallWebClient,
                tgMockCallWebClient,
                urlUserCheck,
                urlUserSubscribe
        );
    }

    @Test
    public void whenUserAlreadyHaveRegisteredThenReturnRejection() {
        when(tgUserService.findByChatId(anyInt())).thenReturn(null);
        String expected = "Для Вашего аккаунта регистрация не выполнена."
                + sl
                + "/start";
        BotApiMethod<Message> res = subscribeAction.handle(message, bindingBy);
        assertThat(res.toString()).contains(expected);
    }

    @Test
    public void whenNoUserRegisteredThenReturnMessageToEnterEmail() {
        when(tgUserService.findByChatId(anyInt())).thenReturn(new TgUser());
        String expected = "Введите email:";
        BotApiMethod<Message> res = subscribeAction.handle(message, bindingBy);
        assertThat(res.toString()).contains(expected);
    }

    @Test
    public void whenEmailOrPasswordAreWrongThenReturnWarningMessage() {
        long chatId = 1L;
        String email = "email.@email.com";
        when(tgUserService.findByChatId(anyInt())).thenReturn(new TgUser());
        when(message.getChatId()).thenReturn(chatId);
        when(message.getText()).thenReturn(email);

        String expected = "Введите password:";
        BotApiMethod<Message> res = subscribeAction.callback(message, bindingBy);
        assertThat(res.toString()).contains(expected);

    }

    @Test
    public void whenEmailAlreadyEnteredThenReturnMessageToEnterPassword() {
        long chatId = 1L;
        String email = "email.@email.com";
        when(tgUserService.findByChatId(anyInt())).thenReturn(new TgUser());
        when(tgAuthCallWebClient.doPost(anyString(), any(PersonDTO.class))).thenReturn(Mono.just(false));
        when(message.getChatId()).thenReturn(chatId);
        when(message.getText()).thenReturn(email);

        call(String.format("Введены неверные данные %s/start", sl));

    }

    @Test
    public void whenAuthServiceIsUnavailableThenReturnWarningMessage() {
        long chatId = 1L;
        String email = "email.@email.com";
        when(tgUserService.findByChatId(anyInt())).thenReturn(new TgUser());
        when(tgAuthCallWebClient.doPost(anyString(), any(PersonDTO.class))).thenThrow(new RuntimeException("SubscribeActionTest"));
        when(message.getChatId()).thenReturn(chatId);
        when(message.getText()).thenReturn(email);

        call(String.format("Сервис недоступен попробуйте позже %s/start", sl));

    }

    @Test
    public void whenWasAlreadySubscribedThenReturnWarningMessage() {
        long chatId = 1L;
        String email = "email.@email.com";
        when(tgUserService.findByChatId(anyInt())).thenReturn(new TgUser());
        when(tgAuthCallWebClient.doPost(anyString(), any(PersonDTO.class))).thenReturn(Mono.just(true));
        when(tgMockCallWebClient.doPost(anyString(), anyLong())).thenThrow(new ConstraintKeyException("ConstraintKeyException - SubscribeActionTest"));
        when(message.getChatId()).thenReturn(chatId);
        when(message.getText()).thenReturn(email);

        call(String.format("Вы ранее уже были подписаны %s/start", sl));

    }

    @Test
    public void whenMockServiceIsUnavailableThenReturnWarningMessage() {
        long chatId = 1L;
        String email = "email.@email.com";
        when(tgUserService.findByChatId(anyInt())).thenReturn(new TgUser());
        when(tgAuthCallWebClient.doPost(anyString(), any(PersonDTO.class))).thenReturn(Mono.just(true));
        when(tgMockCallWebClient.doPost(anyString(), anyLong())).thenThrow(new RuntimeException("Exception - SubscribeActionTest"));
        when(message.getChatId()).thenReturn(chatId);
        when(message.getText()).thenReturn(email);

        call(String.format("Сервис недоступен попробуйте позже %s/start", sl));

    }

    @Test
    public void whenSubscribeThenReturnOkMessage() {
        long chatId = 1L;
        String email = "email.@email.com";
        when(tgUserService.findByChatId(anyInt())).thenReturn(new TgUser());
        when(tgAuthCallWebClient.doPost(anyString(), any(PersonDTO.class))).thenReturn(Mono.just(true));
        when(tgMockCallWebClient.doPost(anyString(), anyLong())).thenReturn(Mono.just(1));
        when(message.getChatId()).thenReturn(chatId);
        when(message.getText()).thenReturn(email);

        call("Вы подписаны");

    }

    private void call(String msg) {
        String expected = "Введите password:";
        BotApiMethod<Message> res = subscribeAction.callback(message, bindingBy);
        assertThat(res.toString()).contains(expected);

        expected = msg;
        res = subscribeAction.callback(message, bindingBy);
        assertThat(res.toString()).contains(expected);
    }


}
