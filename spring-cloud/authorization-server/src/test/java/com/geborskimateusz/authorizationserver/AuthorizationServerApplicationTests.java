package com.geborskimateusz.authorizationserver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"eureka.client.enabled=false"})
@AutoConfigureMockMvc
class AuthorizationServerApplicationTests {

	@Autowired
	MockMvc mockMvc;

	@Test
	public void requestTokenWhenUsingPasswordGrantTypeThenOk() throws Exception {

		this.mockMvc.perform(post("/oauth/token")
				.param("grant_type", "password")
				.param("username", "user")
				.param("password", "password")
				.header("Authorization", "Basic cmVhZGVyOnNlY3JldA=="))
				.andExpect(status().isOk());
	}

	@Test
	public void requestJwkSetWhenUsingDefaultsThenOk()
			throws Exception {

		this.mockMvc.perform(get("/.well-known/jwks.json"))
				.andExpect(status().isOk());
	}


}
