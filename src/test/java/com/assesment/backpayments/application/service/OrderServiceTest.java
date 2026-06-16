package com.assesment.backpayments.application.service;

import com.assesment.backpayments.application.dto.CreateOrderRequest;
import com.assesment.backpayments.application.event.OrderApprovedEvent;
import com.assesment.backpayments.application.exception.IncorrectInvoiceException;
import com.assesment.backpayments.application.exception.InvalidOrderStateException;
import com.assesment.backpayments.domain.model.Order;
import com.assesment.backpayments.domain.model.OrderStatus;
import com.assesment.backpayments.domain.model.User;
import com.assesment.backpayments.infrastructure.security.SecurityUserDetails;
import com.assesment.backpayments.infrastructure.storage.OrderRepository;
import com.assesment.backpayments.infrastructure.storage.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createOrderUsesAuthenticatedUser() {
        OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        InvoiceStorageService invoiceStorageService = Mockito.mock(InvoiceStorageService.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        OrderService service = new OrderService(orderRepository, userRepository, invoiceStorageService, eventPublisher);

        User user = authUser("operator@local");
        setAuthenticatedUser(user);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(UUID.randomUUID());
            return order;
        });

        CreateOrderRequest request = new CreateOrderRequest(new BigDecimal("120.50"), "USD", "test");
        var response = service.createOrder(request);

        assertNotNull(response.id());
        assertEquals(OrderStatus.PENDING, response.status());
        assertEquals(user.getId(), response.createdById());
    }

    @Test
    void approveOrderPublishesEvent() {
        OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        InvoiceStorageService invoiceStorageService = Mockito.mock(InvoiceStorageService.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        OrderService service = new OrderService(orderRepository, userRepository, invoiceStorageService, eventPublisher);

        User admin = authUser("admin@local");
        setAuthenticatedUser(admin);

        Order order = sampleOrder(OrderStatus.PENDING);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.approveOrder(order.getId());

        verify(orderRepository, times(1)).save(order);
        ArgumentCaptor<OrderApprovedEvent> eventCaptor = ArgumentCaptor.forClass(OrderApprovedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(order.getId(), eventCaptor.getValue().orderId());
        assertEquals("admin@local", eventCaptor.getValue().approvedBy());
        assertEquals(OrderStatus.APPROVED, order.getStatus());
    }

    @Test
    void approveOrderFailsWhenNotPending() {
        OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        InvoiceStorageService invoiceStorageService = Mockito.mock(InvoiceStorageService.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        OrderService service = new OrderService(orderRepository, userRepository, invoiceStorageService, eventPublisher);

        setAuthenticatedUser(authUser("admin@local"));
        Order order = sampleOrder(OrderStatus.APPROVED);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStateException.class, () -> service.approveOrder(order.getId()));
    }

    @Test
    void uploadInvoiceRejectsUnsupportedContentType() {
        OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        InvoiceStorageService invoiceStorageService = Mockito.mock(InvoiceStorageService.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        OrderService service = new OrderService(orderRepository, userRepository, invoiceStorageService, eventPublisher);

        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("text/plain");

        assertThrows(IncorrectInvoiceException.class, () -> service.uploadInvoice(UUID.randomUUID(), file));
    }

    @Test
    void downloadInvoiceWithoutKeyFails() {
        OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        InvoiceStorageService invoiceStorageService = Mockito.mock(InvoiceStorageService.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        OrderService service = new OrderService(orderRepository, userRepository, invoiceStorageService, eventPublisher);

        Order order = sampleOrder(OrderStatus.PENDING);
        order.setInvoiceKey(null);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThrows(IncorrectInvoiceException.class, () -> service.downloadInvoice(order.getId()));
    }

    private static void setAuthenticatedUser(User user) {
        SecurityUserDetails userDetails = new SecurityUserDetails(user);
        var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private static User authUser(String email) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash("hash");
        return user;
    }

    private static Order sampleOrder(OrderStatus status) {
        User createdBy = authUser("operator@local");
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setStatus(status);
        order.setAmount(new BigDecimal("10.00"));
        order.setCurrency("USD");
        order.setDescription("desc");
        order.setCreatedAt(Instant.now());
        order.setCreatedBy(createdBy);
        return order;
    }
}
