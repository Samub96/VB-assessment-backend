package com.assesment.backpayments.application.service;

import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

/**
 * Contrato genérico para guardar y recuperar binarios.
 */
public interface StorageService<T> {

    String save(UUID ownerId, MultipartFile file);

    T load(String key);
}
