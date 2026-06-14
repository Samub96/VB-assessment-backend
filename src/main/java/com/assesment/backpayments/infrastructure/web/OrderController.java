package com.assesment.backpayments.infrastructure.web;

import com.assesment.backpayments.application.dto.CreateOrderRequest;
import com.assesment.backpayments.application.dto.OrderResponse;
import com.assesment.backpayments.application.dto.OrderSummaryResponse;
import com.assesment.backpayments.application.service.InvoiceDownload;
import com.assesment.backpayments.application.service.OrderArchivingService;
import com.assesment.backpayments.application.service.OrderService;
import com.assesment.backpayments.domain.model.OrderStatus;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * API de órdenes de pago.
 */
@RestController
@RequestMapping("/api/v1/orders")
@Validated
public class OrderController {

    private final OrderService orderService;
    private final OrderArchivingService orderArchivingService;

    public OrderController(OrderService orderService, OrderArchivingService orderArchivingService) {
        this.orderService = orderService;
        this.orderArchivingService = orderArchivingService;
    }

    @PostMapping
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/api/v1/orders/" + response.id()))
                .body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderSummaryResponse>> list(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdTo
    ) {
        return ResponseEntity.ok(orderService.listOrders(status, currency, minAmount, maxAmount, createdFrom, createdTo));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<OrderResponse> detail(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.approveOrder(id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> reject(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.rejectOrder(id));
    }

    @PostMapping("/archive-rejected")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> archiveRejectedOrders() {
        orderArchivingService.archiveRejectedOrders();
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/invoice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<OrderResponse> uploadInvoice(
            @PathVariable UUID id,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(orderService.uploadInvoice(id, file));
    }

    @GetMapping("/{id}/invoice")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<org.springframework.core.io.Resource> downloadInvoice(@PathVariable UUID id) {
        InvoiceDownload invoice = orderService.downloadInvoice(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(invoice.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(invoice.filename()).build().toString())
                .body(invoice.resource());
    }
}
