package com.events.service;

import com.events.dto.EventDto;
import com.events.entity.Event;

import com.events.parser.DateParser;
import com.events.repository.EventsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventsRepository repository;

    public List<Event> getAllEvents() {
        return repository.findAll();
    }

    public void saveAll(List<EventDto> eventDTOs) {
        for (EventDto dto : eventDTOs) {
            Event event = new Event();
            event.setTitle(dto.getTitle());
            event.setImage(dto.getImage());
            event.setPrice(dto.getPrice());
            event.setLocation(dto.getLocation());
            event.setLink(dto.getLink());

            event.setDateTime(DateParser.parseDate(dto.getDate_time()));
            repository.save(event);
        }
    }
}
