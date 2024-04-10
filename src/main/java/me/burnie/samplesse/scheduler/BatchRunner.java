package me.burnie.samplesse.scheduler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.burnie.samplesse.repository.SseEmitterRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchRunner {

    private final SseEmitterRepository sseEmitterRepository;

    @Scheduled(fixedRate = 5 * 1000L)
    public void checkEmitters(){
        log.info("check event connections with dummy data");
        sseEmitterRepository.checkEvents();
    }




}
