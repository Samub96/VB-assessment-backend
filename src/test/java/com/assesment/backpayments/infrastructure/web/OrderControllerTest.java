package com.assesment.backpayments.infrastructure.web;

import com.assesment.backpayments.application.service.OrderArchivingService;
import com.assesment.backpayments.application.service.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
}
