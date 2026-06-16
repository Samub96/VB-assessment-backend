package com.assesment.backpayments.application.mapper;

import com.assesment.backpayments.application.dto.CreateOrderRequest;
import com.assesment.backpayments.application.dto.OrderResponse;
import com.assesment.backpayments.application.dto.OrderSummaryResponse;
import com.assesment.backpayments.domain.model.Order;
import com.assesment.backpayments.domain.model.OrderStatus;
import com.assesment.backpayments.domain.model.User;
import java.time.Instant;

/**
 * Conversión entre entidades de dominio y DTOs de órdenes.
 */
public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderResponse toResponse(Order order) {
        if (order == null) {
            return null;
        }
        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getAmount(),
                order.getCurrency(),
                order.getDescription(),
                order.getInvoiceKey(),
                order.getCreatedAt(),
                order.getCreatedBy() != null ? order.getCreatedBy().getId() : null
        );
    }

    public static OrderSummaryResponse toSummary(Order order) {
        if (order == null) {
            return null;
        }
        return new OrderSummaryResponse(
                order.getId(),
                order.getStatus(),
                order.getAmount(),
                order.getCurrency(),
                order.getCreatedAt()
        );
    }

    public static Order fromCreateRequest(CreateOrderRequest request, User createdBy) {
        if (request == null) {
            return null;
        }
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        order.setAmount(request.amount());
        order.setCurrency(request.currency());
        order.setDescription(request.description());
        order.setCreatedAt(Instant.now());
        order.setCreatedBy(createdBy);
        return order;
    }
}
