package me.burnie.samplesse.repository;


import lombok.extern.slf4j.Slf4j;
import me.burnie.samplesse.model.RequestDto;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
@Slf4j
public class SseEmitterRepository {
    private final List<SseEmitter> sseEmitters = new CopyOnWriteArrayList<>();

    public SseEmitter add(SseEmitter sseEmitter){
        this.sseEmitters.add(sseEmitter);
        log.info("new emitter added: {}", sseEmitter);
        log.info("emitter list size: {}", sseEmitters.size());

        sseEmitter.onCompletion(() -> {
            log.info("onCompletion callback");
            this.sseEmitters.remove(sseEmitter);    // 만료되면 리스트에서 삭제
        });
        sseEmitter.onTimeout(() -> {
            log.info("onTimeout callback");
            sseEmitter.complete();
        });

        return sseEmitter;
    }

    /**
     * 브라우저가 닫히거나, client단의 sseEventSource가 close() 되어도
     * 따로 추가적인 이벤트를 publish 해주지 않는다면, connection에 대한 정보를 알 수 없다.
     * 고로 SseEmitter와 더불어, tomcat의 connections 또한 유지 된다.
     * 즉, periodic connection check가 필수이다.
     * 해당 방법은, 단순히 connection check 를 위한 dummy data를 event로 동시에 publish하는 방법이다.
     * 결국 사실상, 주기적으로 data가 없을때도, 계속 event를 발행하여 connection에 대한 check를 이어가야 할 듯하다.
     *  방법은 여러가지가 있겠지만, 일단
     *  (1) sseEvent의 timout을 짧게하여, client로 부터 재요청을 줄 수 있도록 하거나,
     *  (2) 아래와 같이 check Event를 계속 발행하여,  connection이 끊긴 emmiters에 대해 AsyncRequestNotUsableException을 throw 하여, remove처리가 되도록 유도하는 것이다.
     */


    public void checkEvents(){
        if(CollectionUtils.isEmpty(sseEmitters))return;
        log.info("checkEvents");
        sseEmitters.forEach(emitter->{
            try {
                emitter.send(SseEmitter.event()
                        .name("connect")
                        .data("check"));
            }catch (Exception e){log.error(e.toString());}
        });
        log.info("emitter checked list size: {}", sseEmitters.size());
    }

    public void updateEvents(RequestDto requestDto){
        if(CollectionUtils.isEmpty(sseEmitters))return;
        log.info("update the Events");
        sseEmitters.forEach(emitter->{
            try {
                emitter.send(SseEmitter.event()
                                .name("quiz")
                                .data(requestDto.toEventData()));
            }catch (Exception e){log.error(e.toString());}
        });
    }


}
