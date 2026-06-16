package com.assesment.backpayments.infrastructure.storage;

import com.assesment.backpayments.domain.model.IntegrationResponseLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio para el registro opcional de integración externa.
 */
public interface IntegrationResponseLogRepository extends JpaRepository<IntegrationResponseLog, UUID> {
}
