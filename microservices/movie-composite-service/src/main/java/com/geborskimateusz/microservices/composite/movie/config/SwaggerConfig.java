package com.geborskimateusz.microservices.composite.movie.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebFlux;

import java.util.Collection;
import java.util.Collections;


@EnableSwagger2WebFlux
@Configuration
public class SwaggerConfig {

    @Value("${api.common.version}")
    private String commonVersion;

    @Value("${api.common.title}")
    private String commonTitle;

    @Value("${api.common.description}")
    private String commonDescription;

    @Value("${api.common.termsOfServiceUrl}")
    private String commonTermsOfServiceUrl;

    @Value("${api.common.license}")
    private String commonLicense;

    @Value("${api.common.licenseUrl}")
    private String commonLicenseUrl;

    @Value("${api.contact.name}")
    private String contactName;

    @Value("${api.contact.url}")
    private String contactUrl;

    @Value("${api.contact.email}")
    private String contactEmail;

    @Bean
    public Docket apiDocumentation() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.geborskimateusz.microservices.composite.movie"))
                .paths(PathSelectors.any())
                .build()
                .globalResponseMessage(RequestMethod.GET, Collections.emptyList())
                .apiInfo(new ApiInfo(
                        commonVersion,
                        commonTitle,
                        commonDescription,
                        commonTermsOfServiceUrl,
                        new Contact(
                                contactName,
                                contactUrl,
                                contactEmail
                        ),
                        commonLicense,
                        commonLicenseUrl,
                        Collections.emptyList()
                ));
    }
}
