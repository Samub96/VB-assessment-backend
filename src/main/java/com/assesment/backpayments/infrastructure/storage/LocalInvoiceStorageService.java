package com.assesment.backpayments.infrastructure.storage;

import com.assesment.backpayments.application.exception.IncorrectInvoiceException;
import com.assesment.backpayments.application.exception.InvoiceNotFoundException;
import com.assesment.backpayments.application.service.InvoiceDownload;
import com.assesment.backpayments.application.service.InvoiceStorageService;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementación local de almacenamiento para facturas.
 */
@Service
public class LocalInvoiceStorageService implements InvoiceStorageService {

    private final Path rootDirectory;

    public LocalInvoiceStorageService(@Value("${app.storage.invoice-root:./data/invoices}") String rootDirectory) {
        this.rootDirectory = Path.of(rootDirectory).toAbsolutePath().normalize();
    }

    @Override
    public String save(UUID orderId, MultipartFile file) {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() == null ? "invoice" : file.getOriginalFilename());
        String extension = StringUtils.getFilenameExtension(originalFilename);
        String invoiceFileName = extension == null || extension.isBlank() ? "invoice" : "invoice." + extension.toLowerCase();
        Path orderDirectory = rootDirectory.resolve(orderId.toString());
        Path invoicePath = orderDirectory.resolve(invoiceFileName);
        Path metadataPath = orderDirectory.resolve("invoice.meta");

        try {
            Files.createDirectories(orderDirectory);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, invoicePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            String metadata = String.join("\n",
                    StringUtils.hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream",
                    originalFilename
            );
            Files.writeString(metadataPath, metadata, StandardCharsets.UTF_8);
            return rootDirectory.relativize(invoicePath).toString().replace('\\', '/');
        } catch (IOException ex) {
            throw new IncorrectInvoiceException("No se pudo almacenar la factura", ex);
        }
    }

    @Override
    public InvoiceDownload load(String key) {
        Path invoicePath = rootDirectory.resolve(key).normalize();
        Path metadataPath = invoicePath.resolveSibling("invoice.meta");
        if (!invoicePath.startsWith(rootDirectory) || !Files.exists(invoicePath)) {
            throw new InvoiceNotFoundException("Factura no encontrada");
        }

        try {
            String contentType = "application/octet-stream";
            String filename = invoicePath.getFileName().toString();
            if (Files.exists(metadataPath)) {
                String metadata = Files.readString(metadataPath, StandardCharsets.UTF_8);
                String[] parts = metadata.split("\n", 2);
                if (parts.length > 0 && StringUtils.hasText(parts[0])) {
                    contentType = parts[0].trim();
                }
                if (parts.length > 1 && StringUtils.hasText(parts[1])) {
                    filename = parts[1].trim();
                }
            }
            Resource resource = new UrlResource(invoicePath.toUri());
            return new InvoiceDownload(contentType, filename, resource);
        } catch (IOException ex) {
            throw new IncorrectInvoiceException("No se pudo leer la factura", ex);
        }
    }
}
