package com.assesment.backpayments.application.service;

import com.assesment.backpayments.infrastructure.storage.OrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderArchivingServiceTest {

    @Test
    void archiveCallsRepositoryOnce() {
        OrderRepository repo = Mockito.mock(OrderRepository.class);
        OrderArchivingService service = new OrderArchivingService(repo);

        service.archiveRejectedOrders();

        Mockito.verify(repo, Mockito.times(1)).archiveRejectedOrders();
    }

    @Test
    void listArchivedOrdersMapsRepositoryResult() {
        OrderRepository repo = Mockito.mock(OrderRepository.class);
        OrderRepository.ArchivedOrderView view = Mockito.mock(OrderRepository.ArchivedOrderView.class);
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(3600);
        Instant archivedAt = Instant.now();
        OffsetDateTime createdAtDb = OffsetDateTime.ofInstant(createdAt, ZoneOffset.UTC);
        OffsetDateTime archivedAtDb = OffsetDateTime.ofInstant(archivedAt, ZoneOffset.UTC);

        Mockito.when(view.getId()).thenReturn(id.toString());
        Mockito.when(view.getStatus()).thenReturn("REJECTED");
        Mockito.when(view.getAmount()).thenReturn(new BigDecimal("19.90"));
        Mockito.when(view.getCurrency()).thenReturn("USD");
        Mockito.when(view.getCreatedAt()).thenReturn(createdAtDb);
        Mockito.when(view.getArchivedAt()).thenReturn(archivedAtDb);
        Mockito.when(repo.listArchivedOrders()).thenReturn(List.of(view));

        OrderArchivingService service = new OrderArchivingService(repo);
        var archived = service.listArchivedOrders();

        assertEquals(1, archived.size());
        assertEquals(id, archived.get(0).id());
        assertEquals(createdAt, archived.get(0).createdAt());
        assertEquals(archivedAt, archived.get(0).archivedAt());
    }
}
