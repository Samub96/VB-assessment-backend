# Pruebas Backend

Este documento resume cómo ejecutar y qué cubren las pruebas del backend.

## Ejecutar pruebas

```bash
./mvnw test
```

## Re-ejecución automática al guardar cambios

```bash
chmod +x scripts/auto-rerun-tests.sh
./scripts/auto-rerun-tests.sh
```

El script monitorea cambios en:
- `src/main/**/*.java`
- `src/test/**/*.java`
- `src/main/**/*.properties`
- `src/main/**/*.sql`

y vuelve a ejecutar la suite automáticamente.

## Estado actual

- Suite backend activa con pruebas unitarias e integración.
- Última ejecución: `BUILD SUCCESS` con **28 tests**.

## Cobertura por módulo

### Seguridad y autenticación
- `AuthServiceTest`
- `CustomUserDetailsServiceTest`
- `DataSeederTest`

### Órdenes (servicio/controlador/reglas)
- `OrderServiceTest`
- `OrderControllerTest`
- `OrderControllerWebMvcTest`

### Archivado y base de datos
- `OrderArchivingServiceTest`
- `OrderArchivingIntegrationTest`
- `OrderArchivingMultipleOrdersTest`
- `OrderRepositoryIntegrationTest`

### Integración externa
- `OrderIntegrationServiceTest`
- `OrderIntegrationListenerTest`
- `DummyIntegrationErrorDecoderTest`

### Storage y errores HTTP
- `LocalInvoiceStorageServiceTest`
- `GlobalExceptionHandlerTest`

### Contexto de aplicación
- `BackPaymentsApplicationTests`

## Nota

Para depurar en detalle, podés usar los niveles de log configurables por variables de entorno (`APP_LOG_LEVEL_*`) documentados en `docs/README-security.md`.
