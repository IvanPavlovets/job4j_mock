package ru.checkdev.notification.telegram.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.exception.ConstraintKeyException;

@Service
@Slf4j
public class TgMockCallWebClient {
    private WebClient webClient;

    public TgMockCallWebClient(@Value("${server.mock}") String urlMock) {
        this.webClient = WebClient.create(urlMock);
    }


    /**
     * Метод POST
     *
     * @param url       URL http
     * @param chatId    Body
     * @return Mono<Object>
     */
    public Mono<Object> doPost(String url, long chatId) {
        return webClient
                .post()
                .uri(url)
                .bodyValue(chatId)
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorMap(e -> {
                    if (e instanceof WebClientResponseException ex) {
                        if (ex.getResponseBodyAsString().contains("constraint [subscribe_chat_id_key]")) {
                            return new ConstraintKeyException("Ошибка базы данных: " + ex.getResponseBodyAsString());
                        }
                    }
                    return e;
                })
                .doOnError(err -> log.error("API not found: {}", err.getMessage()));

    }

    public Mono<Object> doDelete(String url) {
        return webClient
                .delete()
                .uri(url)
                .retrieve()
                .bodyToMono(Object.class)
                .doOnError(err -> log.error("API not found: {}", err.getMessage()));
    }

}
