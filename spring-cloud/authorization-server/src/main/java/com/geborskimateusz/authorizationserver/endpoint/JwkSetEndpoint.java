package com.geborskimateusz.authorizationserver.endpoint;

import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import com.nimbusds.jose.jwk.RSAKey;

@FrameworkEndpoint
public class JwkSetEndpoint {
    KeyPair keyPair;

    public JwkSetEndpoint(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    public Map<String, Object> getKey() {
        RSAPublicKey publicKey = (RSAPublicKey) this.keyPair.getPublic();
        RSAKey key = new RSAKey.Builder(publicKey).build();
        return new JWKSet(key).toJSONObject();
    }
}
