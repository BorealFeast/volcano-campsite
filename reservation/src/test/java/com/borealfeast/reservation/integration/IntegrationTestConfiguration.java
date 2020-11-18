package com.borealfeast.reservation.integration;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.borealfeast")
@Configuration
public class IntegrationTestConfiguration {

    @Bean
    public MockMvc mockMvc(WebApplicationContext context) {
        return MockMvcBuilders.webAppContextSetup(context)
                .defaultRequest(MockMvcRequestBuilders.get("/")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .build();
    }
}