package me.burnie.samplesse.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class RequestDto {
    private Integer round;
    private String question;
    private List<String> answers;

    public String toEventData() {
        StringBuilder builder = new StringBuilder();
        return builder
                .append("round=" + round)
                .append(",question=" + question)
                .append((",answers=") + answers.toString()).toString();

    }
}
