package ru.checkdev.notification.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.checkdev.notification.service.TgUserService;
import ru.checkdev.notification.telegram.action.*;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

import java.util.List;
import java.util.Map;

/**
 * 3. Мидл
 * Инициализация телеграм бот,
 * username = берем из properties
 * token = берем из properties
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 12.09.2023
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class TgRun {
    private final TgAuthCallWebClint tgAuthCallWebClint;
    private final TgUserService tgUserService;
    @Value("${tg.username}")
    private String username;
    @Value("${tg.token}")
    private String token;
    @Value("${server.site.url.login}")
    private String urlSiteAuth;

    @Value("${server.site.url.person}")
    private String urlSitePerson;

    @Value("${server.site.url.change.password}")
    private String urlSiteChangePassword;

    /**
     * 1) TelegramBotsApi - менеджер в котором содержаться
     * все необходимые вызовы с сервисом.
     * 2) botsApi.registerBot(menu) - регистрация бота,
     * каким образом будем управлять вводом данных от польователя.
     * 3) new BotMenu(actionMap, username, token) - реализация
     * TelegramLongPollingBot, создает отдельную нить и начинает
     * слушать события
     */
    @Bean
    public void initTg() {
        Map<String, Action> actionMap = Map.of(
                "/start", new InfoAction(List.of(
                        "/start - Доступные команды",
                        "/new - Зарегистрировать нового пользователя",
                        "/echo - Команды echo",
                        "/check - Выдать ФИО и почту, привязанную к этому аккаунту",
                        "/forget - Восстановить пароль",
                        "/notify - Подписаться на уведомления",
                        "/unnotify - Отписаться от уведомлений"
                )),
                "/new", new RegAction(tgAuthCallWebClint, tgUserService, urlSiteAuth),
                "/echo", new EchoAction("/echo"),
                "/check", new CheckAction(tgUserService, tgAuthCallWebClint, urlSitePerson),
                "/forget", new ForgetAction(tgUserService, tgAuthCallWebClint, urlSiteChangePassword)
//                "/notify", new NotifyAction(tgAuthCallWebClint, urlSiteAuth),
//                "/unnotify", new NotifyAction(tgAuthCallWebClint, urlSiteAuth)
        );
        try {
            BotMenu menu = new BotMenu(actionMap, username, token);

            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(menu);
        } catch (TelegramApiException e) {
            log.error("Telegram bot: {}, ERROR {}", username, e.getMessage());
        }
    }
}
