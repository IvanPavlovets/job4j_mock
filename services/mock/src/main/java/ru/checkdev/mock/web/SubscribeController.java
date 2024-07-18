package ru.checkdev.mock.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.checkdev.mock.domain.Subscribe;
import ru.checkdev.mock.service.SubscribeService;

@RestController
@RequestMapping("/subscribe")
@AllArgsConstructor
@Slf4j
public class SubscribeController {

    private final SubscribeService service;

    @PostMapping("")
    public ResponseEntity<Object> subscribe(@RequestBody long chatId) {
        Subscribe result = service.save(new Subscribe(0, chatId));
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @DeleteMapping("/")
    public ResponseEntity<Boolean> unsubscribe(@RequestBody long chatId) {
        boolean result = service.delete(chatId);
        return new ResponseEntity<>(
                result,
                result ? HttpStatus.OK : HttpStatus.CONFLICT);
    }
}
