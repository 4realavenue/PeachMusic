package com.example.peachmusic.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

@Configuration
public class JamendoRestClientConfig {

    private static final int CONNECTION_TIMEOUT_SECONDS = 1;
    private static final int READ_TIMEOUT_SECONDS = 5;

    private static final Logger log = LoggerFactory.getLogger(JamendoRestClientConfig.class);

    @Bean
    public RestClient jamendoRestClient(RestClient.Builder restClientBuilder, @Value("${jamendo.api.url}") String baseUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(Duration.ofSeconds(CONNECTION_TIMEOUT_SECONDS));

        requestFactory.setReadTimeout(Duration.ofSeconds(READ_TIMEOUT_SECONDS));

        return restClientBuilder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultStatusHandler(
                        statusCode -> statusCode.is4xxClientError() || statusCode.is5xxServerError(),
                        (request, response) -> {
                            log.error("HTTP request failed.");
                            log.error("Request: {} {}", request.getMethod(), request.getURI());
                            log.error("Response: {} {}", response.getStatusCode(), response.getStatusText());

                            if (response.getStatusCode().is4xxClientError()) {
                                throw new RestClientException("Jamendo client exception");
                            }
                            if (response.getStatusCode().is5xxServerError()) {
                                throw new RestClientException("Jamendo server exception");
                            }
                            throw new RestClientException("Unexpected response status: " + response.getStatusCode());
                        }
                )
                .build();
    }
}