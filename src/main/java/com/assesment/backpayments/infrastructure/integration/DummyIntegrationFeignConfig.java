package com.assesment.backpayments.infrastructure.integration;

import feign.Feign;
import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.openfeign.support.SpringMvcContract;

/**
 * Configuración del cliente Feign manual para timeouts y errores HTTP.
 */
@Configuration
public class DummyIntegrationFeignConfig {

    @Bean
    public Request.Options requestOptions(
            @Value("${app.integration.connect-timeout:2s}") Duration connectTimeout,
            @Value("${app.integration.read-timeout:5s}") Duration readTimeout
    ) {
        return new Request.Options(connectTimeout, readTimeout, true);
    }

    @Bean
    public Retryer retryer() {
        return Retryer.NEVER_RETRY;
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new DummyIntegrationErrorDecoder();
    }

    @Bean
    public DummyIntegrationClient dummyIntegrationClient(
            @Value("${app.integration.dummy-url}") String baseUrl,
            Request.Options requestOptions,
            ErrorDecoder errorDecoder,
            JacksonEncoder jacksonEncoder,
            JacksonDecoder jacksonDecoder
    ) {
        return Feign.builder()
                .contract(new SpringMvcContract())
                .encoder(jacksonEncoder)
                .decoder(jacksonDecoder)
                .options(requestOptions)
                .errorDecoder(errorDecoder)
                .retryer(retryer())
                .logLevel(feignLoggerLevel())
                .target(DummyIntegrationClient.class, baseUrl);
    }

    @Bean
    public JacksonEncoder jacksonEncoder() {
        return new JacksonEncoder();
    }

    @Bean
    public JacksonDecoder jacksonDecoder() {
        return new JacksonDecoder();
    }
}
