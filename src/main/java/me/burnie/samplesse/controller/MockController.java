package me.burnie.samplesse.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.burnie.samplesse.model.RequestDto;
import me.burnie.samplesse.repository.SseEmitterRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MockController {

    private final SseEmitterRepository sseEmitterRepository;

    @GetMapping(value = "/sub", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(){
        SseEmitter sseEmitter = new SseEmitter(10 * 1000L);
        sseEmitterRepository.add(sseEmitter);
        try{
            sseEmitter.send(SseEmitter.event().name("connect").data("connected"));
        }catch (Exception e){
           log.error("error!!!");
        }
        return ResponseEntity.ok(sseEmitter);
    }

    @PostMapping(value="/update-quiz")
    public void updateEvent(@RequestBody RequestDto requestDto){
        log.info("update the events");
        log.info("event : " + requestDto.toEventData());
        sseEmitterRepository.updateEvents(requestDto);
    }







}
