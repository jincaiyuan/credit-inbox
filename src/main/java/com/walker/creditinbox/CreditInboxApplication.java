package com.walker.creditinbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class CreditInboxApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditInboxApplication.class, args);
    }

}
