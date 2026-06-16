package com.assesment.backpayments.infrastructure.integration;

import com.assesment.backpayments.application.dto.integration.ExternalApprovalNotificationRequest;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Cliente Feign para el endpoint dummy externo.
 */
public interface DummyIntegrationClient {

    @PostMapping(value = "/post", consumes = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Object> sendApproval(@RequestBody ExternalApprovalNotificationRequest request);
}
