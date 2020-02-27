package com.geborskimateusz.microservices.composite.movie;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * To prevent the OAuth machinery from kicking in when running
 * integration tests.
 */
@TestConfiguration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity){
        httpSecurity.csrf().disable().authorizeExchange().anyExchange().permitAll();
        return httpSecurity.build();
    }

}
