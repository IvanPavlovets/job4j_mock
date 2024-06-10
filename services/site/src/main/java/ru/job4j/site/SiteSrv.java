package ru.job4j.site;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import ru.job4j.site.handler.RestTemplateResponseErrorHandler;

@SpringBootApplication
@Slf4j
public class SiteSrv {
    private static final String SITE = "http://localhost:8080";

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SiteSrv.class);
        application.addListeners(new ApplicationPidFileWriter("./site.pid"));
        application.run();
        log.info("Go to -> :{}", SITE);
    }
}
