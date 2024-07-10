package ru.checkdev.notification.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.checkdev.notification.domain.TgUser;
import ru.checkdev.notification.repository.TgUserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class TgUserService {
    private final TgUserRepository repository;

    public List<TgUser> findAll() {
        return repository.findAll();
    }

    public TgUser findByUserId(int userId) {
        return repository.findByUserId(userId);
    }

    public TgUser findByChatId(int chatId) {
        return repository.findByChatId(chatId);
    }

    public TgUser save(TgUser tgUser) {
        return repository.save(tgUser);
    }

}
