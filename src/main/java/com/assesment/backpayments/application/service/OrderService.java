package com.assesment.backpayments.application.service;

import com.assesment.backpayments.application.dto.CreateOrderRequest;
import com.assesment.backpayments.application.dto.OrderResponse;
import com.assesment.backpayments.application.dto.OrderSummaryResponse;
import com.assesment.backpayments.application.event.OrderApprovedEvent;
import com.assesment.backpayments.application.mapper.OrderMapper;
import com.assesment.backpayments.application.exception.IncorrectInvoiceException;
import com.assesment.backpayments.application.exception.InvalidOrderStateException;
import com.assesment.backpayments.application.exception.OrderNotFoundException;
import com.assesment.backpayments.domain.model.Order;
import com.assesment.backpayments.domain.model.OrderStatus;
import com.assesment.backpayments.domain.model.User;
import com.assesment.backpayments.infrastructure.security.SecurityUserDetails;
import com.assesment.backpayments.infrastructure.storage.OrderRepository;
import com.assesment.backpayments.infrastructure.storage.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Orquesta los casos de uso de órdenes de pago.
 */
@Service
public class OrderService {

    private static final List<String> ALLOWED_INVOICE_CONTENT_TYPES = List.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final InvoiceStorageService invoiceStorageService;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(
            OrderRepository orderRepository,
            UserRepository userRepository,
            InvoiceStorageService invoiceStorageService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.invoiceStorageService = invoiceStorageService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        User currentUser = currentUser();
        Order order = OrderMapper.fromCreateRequest(request, currentUser);
        order.setUpdatedAt(Instant.now());
        return OrderMapper.toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> listOrders(
            OrderStatus status,
            String currency,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Instant createdFrom,
            Instant createdTo
    ) {
        Specification<Order> spec = (root, query, cb) -> cb.conjunction();
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (StringUtils.hasText(currency)) {
            String normalizedCurrency = currency.trim().toUpperCase(Locale.ROOT);
            spec = spec.and((root, query, cb) -> cb.equal(cb.upper(root.get("currency")), normalizedCurrency));
        }
        if (minAmount != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("amount"), minAmount));
        }
        if (maxAmount != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("amount"), maxAmount));
        }
        if (createdFrom != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom));
        }
        if (createdTo != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), createdTo));
        }

        return orderRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(OrderMapper::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID id) {
        return OrderMapper.toResponse(findOrder(id));
    }

    @Transactional
    public OrderResponse approveOrder(UUID id) {
        Order order = findOrder(id);
        validatePending(order);
        User currentUser = currentUser();
        Instant approvedAt = Instant.now();
        order.setStatus(OrderStatus.APPROVED);
        order.setApprovedBy(currentUser);
        order.setApprovedAt(approvedAt);
        order.setUpdatedAt(approvedAt);
        Order savedOrder = orderRepository.save(order);
        eventPublisher.publishEvent(new OrderApprovedEvent(
                savedOrder.getId(),
                savedOrder.getAmount(),
                savedOrder.getCurrency(),
                savedOrder.getDescription(),
                approvedAt,
                currentUser.getEmail()
        ));
        return OrderMapper.toResponse(savedOrder);
    }

    @Transactional
    public OrderResponse rejectOrder(UUID id) {
        Order order = findOrder(id);
        validatePending(order);
        User currentUser = currentUser();
        order.setStatus(OrderStatus.REJECTED);
        order.setRejectedBy(currentUser);
        order.setRejectedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        return OrderMapper.toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse uploadInvoice(UUID id, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IncorrectInvoiceException("La factura no puede estar vacía");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || ALLOWED_INVOICE_CONTENT_TYPES.stream().noneMatch(contentType::equalsIgnoreCase)) {
            throw new IncorrectInvoiceException("La factura debe ser PDF o imagen");
        }

        Order order = findOrder(id);
        String invoiceKey = invoiceStorageService.save(id, file);
        order.setInvoiceKey(invoiceKey);
        order.setUpdatedAt(Instant.now());
        return OrderMapper.toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public InvoiceDownload downloadInvoice(UUID id) {
        Order order = findOrder(id);
        if (!StringUtils.hasText(order.getInvoiceKey())) {
            throw new IncorrectInvoiceException("La orden no tiene factura asociada");
        }
        return invoiceStorageService.load(order.getInvoiceKey());
    }

    private Order findOrder(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Orden no encontrada"));
    }

    private void validatePending(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Solo se pueden modificar órdenes en estado PENDING");
        }
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Usuario no autenticado");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof SecurityUserDetails securityUserDetails) {
            return securityUserDetails.getUser();
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Usuario autenticado no encontrado"));
    }
}
