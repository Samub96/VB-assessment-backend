package com.assesment.backpayments.infrastructure.integration;

import com.assesment.backpayments.application.exception.ExternalIntegrationClientException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DummyIntegrationErrorDecoderTest {

    @Test
    void decodeCreatesExternalIntegrationClientException() {
        DummyIntegrationErrorDecoder decoder = new DummyIntegrationErrorDecoder();
        Request request = Request.create(
                Request.HttpMethod.POST,
                "https://dummyjson.com/post",
                Map.of(),
                new byte[0],
                StandardCharsets.UTF_8,
                new RequestTemplate()
        );
        Response response = Response.builder()
                .status(502)
                .reason("bad gateway")
                .request(request)
                .body("{\"error\":\"down\"}", StandardCharsets.UTF_8)
                .build();

        Exception ex = decoder.decode("DummyIntegrationClient#sendApproval", response);

        ExternalIntegrationClientException typed = assertInstanceOf(ExternalIntegrationClientException.class, ex);
        assertEquals(502, typed.getStatusCode());
        assertEquals("{\"error\":\"down\"}", typed.getResponseBody());
    }
}
