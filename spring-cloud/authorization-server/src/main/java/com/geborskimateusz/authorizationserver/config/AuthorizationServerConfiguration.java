package com.geborskimateusz.authorizationserver.config;

import com.geborskimateusz.authorizationserver.config.utils.SubjectAttributeUserTokenConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.security.KeyPair;

@EnableAuthorizationServer
@Configuration
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    AuthenticationManager authenticationManager;
    KeyPair keyPair;
    boolean jwtEnabled;

    public AuthorizationServerConfiguration(
            AuthenticationConfiguration authenticationConfiguration,
            KeyPair keyPair,
            @Value("${security.oauth2.authorizationserver.jwt.enabled:true}") boolean jwtEnabled) throws Exception {
        this.authenticationManager = authenticationConfiguration.getAuthenticationManager();
        this.keyPair = keyPair;
        this.jwtEnabled = jwtEnabled;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {

        // @formatter:off
        clients.inMemory()
                        .withClient("reader")
                        .authorizedGrantTypes("code", "authorization_code", "implicit", "password")
                        .redirectUris("http://my.redirect.uri")
                        .secret("{noop}secret")
                        .scopes("movie:read")
                        .accessTokenValiditySeconds(600_000_000)
                .and()
                        .withClient("writer")
                        .authorizedGrantTypes("code", "authorization_code", "implicit", "password")
                        .redirectUris("http://my.redirect.uri")
                        .secret("{noop}secret")
                        .scopes("movie:read", "movie:write")
                        .accessTokenValiditySeconds(600_000_000)
                .and()
                        .withClient("noscopes")
                        .authorizedGrantTypes("code", "authorization_code", "implicit", "password")
                        .redirectUris("http://my.redirect.uri")
                        .secret("{noop}secret")
                        .scopes("none")
                        .accessTokenValiditySeconds(600_000_000);
        // @formatter:on
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
                .authenticationManager(this.authenticationManager)
                .tokenStore(tokenStore());

        if (jwtEnabled)
            endpoints.accessTokenConverter(accessTokenConverter());
    }

    @Bean
    public TokenStore tokenStore() {
        return this.jwtEnabled ?
                new JwtTokenStore(accessTokenConverter())
                : new InMemoryTokenStore();
    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setKeyPair(this.keyPair);

        DefaultAccessTokenConverter accessTokenConverter = new DefaultAccessTokenConverter();
        accessTokenConverter.setUserTokenConverter(new SubjectAttributeUserTokenConverter());
        converter.setAccessTokenConverter(accessTokenConverter);

        return converter;
    }
}
