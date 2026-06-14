package com.assesment.backpayments.application.service;

import com.assesment.backpayments.infrastructure.storage.OrderRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class OrderArchivingServiceTest {

    @Test
    void archiveCallsRepositoryOnce() {
        OrderRepository repo = Mockito.mock(OrderRepository.class);
        OrderArchivingService service = new OrderArchivingService(repo);

        service.archiveRejectedOrders();

        Mockito.verify(repo, Mockito.times(1)).archiveRejectedOrders();
    }
}
