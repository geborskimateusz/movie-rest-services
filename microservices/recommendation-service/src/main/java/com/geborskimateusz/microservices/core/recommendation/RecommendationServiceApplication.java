package com.geborskimateusz.microservices.core.recommendation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@SpringBootApplication
@ComponentScan({"com.geborskimateusz"})
public class RecommendationServiceApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(RecommendationServiceApplication.class, args);

		String mongodDbHost = context.getEnvironment().getProperty("spring.data.mongodb.host");
		String mongodDbPort = context.getEnvironment().getProperty("spring.data.mongodb.port");
		log.info("Connected to MongoDb: " + mongodDbHost + ":" + mongodDbPort);
	}

}
