package com.posty.postingapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class PostingApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PostingApiApplication.class, args);
    }

}
