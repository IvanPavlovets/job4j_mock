package ru.checkdev.notification.telegram.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.PersonDTO;

import java.util.Map;

@Service
@Slf4j
public class TgMockCallWebClint {
    private WebClient webClient;

    public TgMockCallWebClint(@Value("${server.mock}") String urlMock) {
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
                .doOnError(err -> log.error("API not found: {}", err.getMessage()));
    }

    public void setWebClient(WebClient webClient) {
        this.webClient = webClient;
    }

}
