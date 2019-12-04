package com.geborskimateusz.microservices.core.movie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@SpringBootApplication
@ComponentScan("com.geborskimateusz")
public class MovieServiceApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(MovieServiceApplication.class, args);

        String mongodDbHost = context.getEnvironment().getProperty("spring.data.mongodb.host");
        String mongodDbPort = context.getEnvironment().getProperty("spring.data.mongodb.port");
        log.info("Connected to MongoDb: " + mongodDbHost + ":" + mongodDbPort);
    }

}
