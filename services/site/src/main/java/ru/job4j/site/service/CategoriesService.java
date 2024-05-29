package ru.job4j.site.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.job4j.site.domain.StatusInterview;
import ru.job4j.site.dto.CategoryDTO;
import ru.job4j.site.dto.InterviewDTO;
import ru.job4j.site.dto.TopicDTO;

import java.util.List;

@AllArgsConstructor
@Service
public class CategoriesService {
    private final TopicsService topicsService;
    private final InterviewsService interviewsService;

    public List<CategoryDTO> getAll() throws JsonProcessingException {
        var text = new RestAuthCall("http://localhost:9902/categories/").get();
        var mapper = new ObjectMapper();
        List<CategoryDTO> categoryDTOList = mapper.readValue(text, new TypeReference<>() {
        });
        return setNewInterviewNumber(categoryDTOList);
    }

    public List<CategoryDTO> getPopularFromDesc() throws JsonProcessingException {
        var text = new RestAuthCall("http://localhost:9902/categories/most_pop").get();
        var mapper = new ObjectMapper();
        List<CategoryDTO> categoryDTOList = mapper.readValue(text, new TypeReference<>() {
        });
        return setNewInterviewNumber(categoryDTOList);
    }

    public CategoryDTO create(String token, CategoryDTO category) throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var out = new RestAuthCall("http://localhost:9902/category/").post(
                token,
                mapper.writeValueAsString(category)
        );
        return mapper.readValue(out, CategoryDTO.class);
    }

    /**
     * Устанавливаем количество новых интерьвью newTopicSize, для модли categoryDTO.
     * 1) Получаем List<InterviewDTO> только тех чей статус "IS_NEW" (id = 1).
     * 2) во внутренем цикле получаем List<TopicDTO> и увеличиваем счетчик count,
     * если поле topicId (interviewDTO) совподает с id (topicDTO)
     * 3) во внешнем цикле устанавливаем поле newTopicSize для каждого categoryDTO
     * из categoryDTOList
     *
     * @param categoryDTOList - список categoryDTO
     * @return List<CategoryDTO>
     * @throws JsonProcessingException
     */
    private List<CategoryDTO> setNewInterviewNumber(List<CategoryDTO> categoryDTOList) throws JsonProcessingException {
        List<InterviewDTO> interviewDTOList = interviewsService.getByStatus(StatusInterview.IS_NEW.getId());
        for (CategoryDTO categoryDTO : categoryDTOList) {
            long count = 0;
            for (TopicDTO topicDTO : topicsService.getByCategory(categoryDTO.getId())) {
                count += interviewDTOList.stream()
                        .filter(InterviewDTO -> InterviewDTO.getTopicId() == topicDTO.getId())
                        .count();
            }
            categoryDTO.setNewTopicSize((int) count);
        }
        return categoryDTOList;
    }

    public void update(String token, CategoryDTO category) throws JsonProcessingException {
        var mapper = new ObjectMapper();
        new RestAuthCall("http://localhost:9902/category/").put(
                token,
                mapper.writeValueAsString(category)
        );
    }

    public void updateStatistic(String token, int categoryId) throws JsonProcessingException {
        var mapper = new ObjectMapper();
        new RestAuthCall("http://localhost:9902/category/statistic").put(
                token, mapper.writeValueAsString(categoryId));
    }

    /**
     * В цикле установливаем значения поля topicsSize, модели CategoryDTO,
     * разбиение тем по категориям, т.е. количество категорий (из -
     * Java Base, Java Core, Spring) встречающихся в темах (модель TopicDTO)
     *
     * @return возварщает количество CategoryDTO в каждом TopicDTO
     * @throws JsonProcessingException
     */
    public List<CategoryDTO> getAllWithTopics() throws JsonProcessingException {
        var categoriesDTO = getAll();
        for (var categoryDTO : categoriesDTO) {
            categoryDTO.setTopicsSize(topicsService.getByCategory(categoryDTO.getId()).size());
        }
        return setNewInterviewNumber(categoriesDTO);
    }

    /**
     * В методе устанвливаем поле topicsSize,
     * для каждого categoryDTO (Java Base, Java Core, Spring)
     * topicsSize - это сколько раз категория встречаеться в темах,
     * разбиение тем по категориям.
     *
     * @return
     * @throws JsonProcessingException
     */
    public List<CategoryDTO> getMostPopular() throws JsonProcessingException {
        var categoriesDTO = getPopularFromDesc();
        for (var categoryDTO : categoriesDTO) {
            categoryDTO.setTopicsSize(topicsService.getByCategory(categoryDTO.getId()).size());
        }
        return categoriesDTO;
    }

    public String getNameById(List<CategoryDTO> list, int id) {
        String result = "";
        for (CategoryDTO category : list) {
            if (id == category.getId()) {
                result = category.getName();
                break;
            }
        }
        return result;
    }
}
