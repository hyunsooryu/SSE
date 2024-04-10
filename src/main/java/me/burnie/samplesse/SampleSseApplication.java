package me.burnie.samplesse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
public class SampleSseApplication {

	public static void main(String[] args) {

		SpringApplication.run(SampleSseApplication.class, args);
	}

}
