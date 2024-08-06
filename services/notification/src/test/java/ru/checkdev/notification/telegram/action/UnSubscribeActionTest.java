package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import reactor.core.publisher.Mono;
import ru.checkdev.notification.telegram.service.TgMockCallWebClient;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

public class UnSubscribeActionTest {

    private UnsubscribeAction unsubscribeAction;
    private TgMockCallWebClient tgMockCallWebClient;

    private Message message = mock(Message.class);

    private Map<String, String> bindingBy = new HashMap<>();

    private String sl = System.lineSeparator();
    private final String urlUserSubscribe = "urlUserSubscribe";

    @BeforeEach
    void init() {
        tgMockCallWebClient = mock((TgMockCallWebClient.class));
        unsubscribeAction = new UnsubscribeAction(tgMockCallWebClient, urlUserSubscribe);
    }

    @Test
    public void whenMockServiceIsUnAvailableThenReturnWarningMessage() {
        when(tgMockCallWebClient.doDelete(anyString())).thenThrow(new RuntimeException("RuntimeException - UnSubscribeActionTest"));
        String expected = String.format("Сервис не доступен попробуйте позже %s/start", sl);

        BotApiMethod<Message> res = unsubscribeAction.handle(message, bindingBy);
        assertThat(res.toString()).contains(expected);
    }

    @Test
    public void whenUnsubscribeOkThenReturnOkMessage() {
        when(tgMockCallWebClient.doDelete(anyString())).thenReturn(Mono.just(1));
        String expected = "Отписка успешно выполнена";

        BotApiMethod<Message> res = unsubscribeAction.handle(message, bindingBy);
        assertThat(res.toString()).contains(expected);
    }

    @Test
    public void whenItWasNotSubscribeThenReturnNoSubscribe() {
        when(tgMockCallWebClient.doDelete(anyString())).thenReturn(Mono.just(0));
        String expected = "Вы не были подписаны";

        BotApiMethod<Message> res = unsubscribeAction.handle(message, bindingBy);
        assertThat(res.toString()).contains(expected);
    }


}
