package com.example.EventRegister.controller;

import com.example.EventRegister.model.Event;
import com.example.EventRegister.model.Response;
import com.example.EventRegister.service.EventService;
import com.example.EventRegister.service.ResponseService;
import com.example.EventRegister.repository.UserRepository;
import com.example.EventRegister.model.User;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/participant")
public class ParticipantController {

    private final EventService eventService;
    private final ResponseService responseService;
    private final UserRepository userRepo;

    public ParticipantController(EventService eventService, ResponseService responseService, UserRepository userRepo) {
        this.eventService = eventService;
        this.responseService = responseService;
        this.userRepo = userRepo;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("events", eventService.getAllEvents());
        return "participant/dashboard";
    }

    @GetMapping("/event/{id}")
    public String showEventForm(@PathVariable String id, Model model, Authentication auth) {
        Optional<Event> eventOpt = eventService.getEventById(id);
        if (eventOpt.isEmpty()) return "redirect:/participant/dashboard";

        String userEmail = auth.getName();
        String userId = userRepo.findByEmail(userEmail).orElseThrow().getId();

        if (responseService.hasResponded(userId, id)) {
            return "redirect:/participant/my-events";
        }

        model.addAttribute("event", eventOpt.get());
        model.addAttribute("response", new Response());
        return "participant/event-form";
    }

    @PostMapping("/event/{id}")
    public String submitResponse(@PathVariable String id, @ModelAttribute Response response, Authentication auth) {
        String userEmail = auth.getName();
        User user = userRepo.findByEmail(userEmail).orElseThrow();
        responseService.saveResponse(response, user.getId(), id);
        return "redirect:/participant/my-events";
    }

    @GetMapping("/my-events")
    public String viewJoinedEvents(Model model, Authentication auth) {
        String userEmail = auth.getName();
        User user = userRepo.findByEmail(userEmail).orElseThrow();
        model.addAttribute("responses", responseService.getResponsesByUser(user.getId()));
        model.addAttribute("eventsMap", eventService.mapEventIdsToTitles());
        return "participant/my-events";
    }
}

