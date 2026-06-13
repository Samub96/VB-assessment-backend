package com.assesment.backpayments.infrastructure.integration;

import com.assesment.backpayments.application.exception.ExternalIntegrationClientException;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import feign.Util;

/**
 * Convierte respuestas 4xx/5xx en excepciones con detalle legible.
 */
public class DummyIntegrationErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        String body = null;
        if (response.body() != null) {
            try {
                body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
            } catch (IOException ignored) {
                body = null;
            }
        }
        return new ExternalIntegrationClientException(
                response.status(),
                "Servicio externo respondió con estado " + response.status(),
                body
        );
    }
}
