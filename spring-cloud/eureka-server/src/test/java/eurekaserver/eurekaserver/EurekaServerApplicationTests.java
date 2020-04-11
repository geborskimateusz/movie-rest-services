package eurekaserver.eurekaserver;

import com.netflix.ribbon.proxy.annotation.Http;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.cloud.config.enabled=false","management.health.rabbit.enabled=false"})
class EurekaServerApplicationTests {

    @Test
    void contextLoads() {
    }

    @Value("${app.eureka-username}")
    private String username;

    @Value("${app.eureka-password}")
    private String password;

    @Autowired
    public void setRestTemplate(TestRestTemplate template) {
        this.testRestTemplate = template.withBasicAuth(username, password);
    }

    private TestRestTemplate testRestTemplate;

    @Test
    public void catalogLoads() {

        String expectedReponseBody = "{\"applications\":{\"versions__delta\":\"1\",\"apps__hashcode\":\"\",\"application\":[]}}";
        ResponseEntity<String> entity = testRestTemplate.getForEntity("/eureka/apps", String.class);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(expectedReponseBody, entity.getBody());
    }

    @Test
    public void statusUp() {
        String expectedBody = "{\"status\":\"UP\"}";
        ResponseEntity<String> response = testRestTemplate.getForEntity("/actuator/health", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedBody, response.getBody());
    }
}
