package com.geborskimateusz.microservices.composite.movie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;

@SpringBootApplication
@ComponentScan({"com.geborskimateusz"})
public class MovieCompositeServiceApplication {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    public static void main(String[] args) {
        SpringApplication.run(MovieCompositeServiceApplication.class, args);
    }

}
