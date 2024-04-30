package ru.job4j.site.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.job4j.site.dto.InterviewDTO;
import ru.job4j.site.dto.ProfileDTO;
import ru.job4j.site.service.*;

import javax.servlet.http.HttpServletRequest;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.job4j.site.controller.RequestResponseTools.getToken;

@Controller
@AllArgsConstructor
@Slf4j
public class IndexController {
    private final CategoriesService categoriesService;
    private final InterviewsService interviewsService;
    private final AuthService authService;
    private final ProfilesService profilesService;
    private final NotificationService notifications;

    @GetMapping({"/", "index"})
    public String getIndexPage(Model model, HttpServletRequest req,
                               @RequestParam(required = false, defaultValue = "0") int page,
                               @RequestParam(required = false, defaultValue = "20") int size) throws JsonProcessingException {
        RequestResponseTools.addAttrBreadcrumbs(model,
                "Главная", "/"
        );
        try {
            model.addAttribute("categories", categoriesService.getMostPopular());
            var token = getToken(req);
            Page<InterviewDTO> interviewsPage = interviewsService.getAll(token, page, size);
            model.addAttribute("new_interviews", interviewsPage);
            Set<ProfileDTO> userList = interviewsPage.toList().stream()
                    .map(x -> profilesService.getProfileById(x.getSubmitterId()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            model.addAttribute("users", userList);
            if (token != null) {
                var userInfo = authService.userInfo(token);
                model.addAttribute("userDTO", notifications.findCategoriesByUserId(userInfo.getId()));
                model.addAttribute("userInfo", userInfo);
                RequestResponseTools.addAttrCanManage(model, userInfo);
            }
        } catch (Exception e) {
            log.error("Remote application not responding. Error: {}. {}, ", e.getCause(), e.getMessage());
        }
        return "index";
    }

}