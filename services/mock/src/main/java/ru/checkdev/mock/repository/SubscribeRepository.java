package ru.checkdev.mock.repository;

import org.springframework.data.repository.CrudRepository;
import ru.checkdev.mock.domain.Subscribe;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;

public interface SubscribeRepository extends CrudRepository<Subscribe, Long> {

    @NotNull
    Subscribe save(Subscribe subscribe);

    @Transactional
    int deleteByChatId(long id);
}
