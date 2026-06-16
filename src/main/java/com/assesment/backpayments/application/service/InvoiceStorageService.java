package com.assesment.backpayments.application.service;

/**
 * Contrato de almacenamiento de facturas desacoplado del proveedor físico.
 */
public interface InvoiceStorageService extends StorageService<InvoiceDownload> {
}
