package com.events.controller;

import com.events.dto.EventDto;
import com.events.entity.Event;
import com.events.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
@CrossOrigin(origins = "*")
public class EventController {

    @Autowired
    private EventService eventService;

    @GetMapping
    public List<Event> getEvents() {
        return eventService.getAllEvents();
    }

    @PostMapping("/add")
    public String addEvents(@RequestBody List<EventDto> events) {
        eventService.saveAll(events);
        return events.size() + " events have been successfully added!";
    }
}
