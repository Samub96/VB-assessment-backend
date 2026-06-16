package com.assesment.backpayments.infrastructure.storage;

import com.assesment.backpayments.application.exception.InvoiceNotFoundException;
import com.assesment.backpayments.application.service.InvoiceDownload;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalInvoiceStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void saveAndLoadInvoiceRoundTrip() throws IOException {
        LocalInvoiceStorageService service = new LocalInvoiceStorageService(tempDir.toString());
        UUID orderId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invoice.pdf",
                "application/pdf",
                "dummy-content".getBytes()
        );

        String key = service.save(orderId, file);
        InvoiceDownload download = service.load(key);

        assertEquals("application/pdf", download.contentType());
        assertEquals("invoice.pdf", download.filename());
        assertTrue(download.resource().exists());
        assertEquals("dummy-content", Files.readString(Path.of(download.resource().getURI())));
    }

    @Test
    void loadRejectsPathTraversal() {
        LocalInvoiceStorageService service = new LocalInvoiceStorageService(tempDir.toString());
        assertThrows(InvoiceNotFoundException.class, () -> service.load("../outside.pdf"));
    }
}
