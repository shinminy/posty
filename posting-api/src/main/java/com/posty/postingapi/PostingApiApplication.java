package com.posty.postingapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PostingApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PostingApiApplication.class, args);
    }

}
