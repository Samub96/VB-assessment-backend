package com.assesment.backpayments.infrastructure.web;

import com.assesment.backpayments.application.dto.ArchivedOrderSummaryResponse;
import com.assesment.backpayments.application.service.OrderArchivingService;
import com.assesment.backpayments.application.service.OrderService;
import com.assesment.backpayments.domain.model.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderControllerTest {

    @Test
    void archiveEndpointInvokesServiceAndReturnsNoContent() {
        OrderService orderService = Mockito.mock(OrderService.class);
        OrderArchivingService archivingService = Mockito.mock(OrderArchivingService.class);
        OrderController controller = new OrderController(orderService, archivingService);

        ResponseEntity<Void> response = controller.archiveRejectedOrders();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(archivingService, times(1)).archiveRejectedOrders();
    }

    @Test
    void listArchivedOrdersReturnsBodyFromService() {
        OrderService orderService = Mockito.mock(OrderService.class);
        OrderArchivingService archivingService = Mockito.mock(OrderArchivingService.class);
        OrderController controller = new OrderController(orderService, archivingService);
        var item = new ArchivedOrderSummaryResponse(
                UUID.randomUUID(),
                OrderStatus.REJECTED,
                new BigDecimal("100.00"),
                "USD",
                Instant.now().minusSeconds(60),
                Instant.now()
        );
        when(archivingService.listArchivedOrders()).thenReturn(List.of(item));

        ResponseEntity<List<ArchivedOrderSummaryResponse>> response = controller.listArchivedOrders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }
}
