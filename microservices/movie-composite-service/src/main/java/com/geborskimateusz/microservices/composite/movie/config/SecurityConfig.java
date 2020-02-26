package com.geborskimateusz.microservices.composite.movie.config;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain springSecurityWebFilterChain(ServerHttpSecurity http) {

        /**
         * By convention, OAuth 2.0 scopes should be prefixed with SCOPE_ when
         * checked for authority using Spring Security.
         */
        http
                .authorizeExchange()
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers(HttpMethod.POST, "/movie-composite/**")
                .hasAnyAuthority("SCOPE_movie:write")
                .pathMatchers(HttpMethod.DELETE, "/movie-composite/**")
                .hasAnyAuthority("SCOPE_movie:write")
                .pathMatchers(HttpMethod.GET, "/movie-composite/**")
                .hasAnyAuthority("SCOPE_movie:read")
                .anyExchange().authenticated()
                .and()
                .oauth2ResourceServer()
                .jwt();

        return http.build();
    }
}
