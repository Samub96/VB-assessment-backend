package com.assesment.backpayments.application.service;

import org.springframework.core.io.Resource;

/**
 * Representa una factura lista para descargar.
 */
public record InvoiceDownload(String contentType, String filename, Resource resource) {
}
