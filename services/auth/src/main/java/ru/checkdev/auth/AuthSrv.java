package ru.checkdev.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@Slf4j
public class AuthSrv {
    private static final String SITE = "http://localhost:9900";

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(AuthSrv.class);
        application.addListeners(new ApplicationPidFileWriter("./auth.pid"));
        application.run();
        log.info("Go to -> :{}", SITE);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
