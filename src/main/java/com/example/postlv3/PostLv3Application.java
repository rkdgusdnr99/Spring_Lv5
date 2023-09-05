package com.example.postlv3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing

public class PostLv3Application {

    public static void main(String[] args) {
        SpringApplication.run(PostLv3Application.class, args);
    }

}
