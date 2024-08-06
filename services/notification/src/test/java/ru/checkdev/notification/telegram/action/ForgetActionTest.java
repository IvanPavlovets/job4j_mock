package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.domain.TgUser;
import ru.checkdev.notification.service.TgUserService;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClient;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ForgetActionTest {

    private ForgetAction forgetAction;
    private TgUserService tgUserService;
    private TgAuthCallWebClient tgAuthCallWebClient;
    private Message message;
    private Map<String, String> bindingBy = new HashMap<>();
    private String sl = System.lineSeparator();
    private String urlSiteChangePassword = "";

    @BeforeEach
    void init() {
        tgUserService = mock(TgUserService.class);
        tgAuthCallWebClient = mock((TgAuthCallWebClient.class));
        message = mock(Message.class);
        forgetAction = new ForgetAction(tgUserService, tgAuthCallWebClient, urlSiteChangePassword);
    }

    @Test
    public void whenNotRegisteredThenReturnDemandToRegister() {
        when(tgUserService.findByChatId(anyInt())).thenReturn(null);
        String expected = "Для Вашего аккаунта регистрация не выполнена."
                + sl
                + "/start";
        BotApiMethod<Message> res = forgetAction.handle(message, bindingBy);
        assertThat(res.toString()).contains(expected);
    }

    @Test
    public void whenAuthServiceIsUnavailableThenReturnDemandToWait() {
        when(tgUserService.findByChatId(anyInt())).thenReturn(new TgUser());
        when(tgAuthCallWebClient.doPost(anyString(), any(PersonDTO.class))).thenThrow(RuntimeException.class);
        String expected = "Сервис не доступен попробуйте позже." + sl
                + "/start";
        BotApiMethod<Message> res = forgetAction.handle(message, bindingBy);
        assertThat(res.toString()).contains(expected);
    }

    @Test
    public void whenSuccessRequestThenReturnNameAndEmail() {
        ArgumentCaptor<PersonDTO> personDTOArgumentCaptor = ArgumentCaptor.forClass(PersonDTO.class);

        when(tgUserService.findByChatId(anyInt())).thenReturn(new TgUser());
        when(tgAuthCallWebClient.doPost(anyString(), personDTOArgumentCaptor.capture())).thenReturn(Mono.just(new HashMap<>()));
        BotApiMethod<Message> res = forgetAction.handle(message, bindingBy);
        String expected = "Новый пароль: " + personDTOArgumentCaptor.getValue().getPassword();
        assertThat(res.toString()).contains(expected);
    }

}
