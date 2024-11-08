package ru.job4j.site.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.job4j.site.dto.SubscribeCategory;
import ru.job4j.site.dto.SubscribeTopicDTO;
import ru.job4j.site.dto.UserDTO;
import ru.job4j.site.dto.UserTopicDTO;

import java.util.List;

@Service
public class NotificationService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String notificationServiceUrl;
    public NotificationService(KafkaTemplate<String, Object> kafkaTemplate,
                               @Value("${service.notification}") String notificationServiceUrl) {
        this.kafkaTemplate = kafkaTemplate;
        this.notificationServiceUrl = notificationServiceUrl;
    }

    public void addSubscribeCategory(int userId, int categoryId) {
        SubscribeCategory subscribeCategory = new SubscribeCategory(userId, categoryId);
        kafkaTemplate.send("subscribeCategory_add", subscribeCategory);
    }

    public void deleteSubscribeCategory(int userId, int categoryId) {
        SubscribeCategory subscribeCategory = new SubscribeCategory(userId, categoryId);
        kafkaTemplate.send("subscribeCategory_delete", subscribeCategory);
    }

    public UserDTO findCategoriesByUserId(int id) throws JsonProcessingException {
        var text = new RestAuthCall(notificationServiceUrl + "/subscribeCategory/" + id).get();
        var mapper = new ObjectMapper();
        List<Integer> list = mapper.readValue(text, new TypeReference<>() {
        });
        return new UserDTO(id, list);
    }

    public void addSubscribeTopic(int userId, int topicId) {
        SubscribeTopicDTO subscribeTopicDTO = new SubscribeTopicDTO(userId, topicId);
        kafkaTemplate.send("subscribeTopic_add", subscribeTopicDTO);
    }

    public void deleteSubscribeTopic(int userId, int topicId) {
        SubscribeTopicDTO subscribeTopic = new SubscribeTopicDTO(userId, topicId);
        kafkaTemplate.send("subscribeTopic_delete", subscribeTopic);
    }

    public UserTopicDTO findTopicByUserId(int id) throws JsonProcessingException {
        var text = new RestAuthCall(notificationServiceUrl + "/subscribeTopic/" + id).get();
        var mapper = new ObjectMapper();
        List<Integer> list = mapper.readValue(text, new TypeReference<>() {
        });
        return new UserTopicDTO(id, list);
    }
}