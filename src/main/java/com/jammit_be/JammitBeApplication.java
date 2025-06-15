package com.jammit_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JammitBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(JammitBeApplication.class, args);
    }

}
