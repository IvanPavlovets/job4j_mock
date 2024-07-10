package ru.checkdev.notification.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.checkdev.notification.domain.TgUser;

import java.util.List;

@Repository
public interface TgUserRepository extends CrudRepository<TgUser, Integer> {
    List<TgUser> findAll();

    TgUser findByUserId(int userId);

    TgUser findByChatId(int chatId);

}
