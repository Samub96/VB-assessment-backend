IMPLEMENTATION NOTES — DB & ARCHIVING, TESTS, DOCKER

Resumen de lo implementado (resumen ejecutable):

1) Esquema y lógica DB
- Archivos de esquema por plataforma:
  - src/main/resources/schema-h2.sql  (H2 - usado en tests)
  - src/main/resources/schema-postgresql.sql  (Postgres - prod)
- Auditoría: trigger en orders que registra cambios en order_status_log.
- Archivado: procedimiento archive_rejected_orders (Postgres PL/pgSQL y alias Java para H2). Se mueve órdenes REJECTED a archived_orders y archived_order_status_log dentro de una transacción.
- Clases Java que soportan H2 runtime:
  - com.assesment.backpayments.infrastructure.database.OrderStatusAuditTrigger (org.h2.api.Trigger)
  - com.assesment.backpayments.infrastructure.database.RejectedOrderArchiveProcedure (método estático archiveRejectedOrders(Connection))

2) Servicio y API
- Servicio: com.assesment.backpayments.application.service.OrderArchivingService
  - Método: archiveRejectedOrders() marcado @Transactional que delega al repositorio.
- Repositorio: OrderRepository tiene método nativo @Modifying @Query(value = "CALL archive_rejected_orders()", nativeQuery = true)
- Endpoint administrativo:
  - POST /api/v1/orders/archive-rejected  (OrderController.archiveRejectedOrders)
  - Requiere rol ADMIN (@PreAuthorize)

3) Tests añadidos
- Unitarios (Mockito):
  - OrderArchivingServiceTest (verifica llamada al repositorio)
  - OrderControllerTest (invoca controller directamente y verifica respuesta)
- Integración/H2:
  - OrderArchivingIntegrationTest (inserta usuario, orden REJECTED y log; invoca service; verifica tablas archived_*)
  - OrderArchivingMultipleOrdersTest (misma idea para múltiples órdenes)
  - OrderRepositoryIntegrationTest (invoca repo.archiveRejectedOrders() y verifica resultados)

4) Docker / despliegue
- Dockerfile multi-stage (maven build + runtime JRE 17). Usa mvn para construir y copia JAR final a imagen runtime.
- docker-compose.yml incluido: levanta postgres:15 + app. Variables importantes definidas en compose:
  - SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD, APP_DB_PLATFORM=postgresql, APP_SEED_ENABLED=true
- .dockerignore creado; ojo: si usas mvnw y .mvn, asegúrate de no ignorar .mvn en .dockerignore o cambiar Dockerfile para no depender de mvnw.

5) Cómo ejecutar localmente
- Tests (rápidos): ./mvnw test
- Ejecutar app sin Docker: ./mvnw spring-boot:run  (http://localhost:8080)
- Docker (dev): docker-compose up --build  (app en http://localhost:8080)
- Construir imagen: docker build -t backpayments:latest .
  - Run: docker run -e SPRING_DATASOURCE_URL=jdbc:postgresql://<db>:5432/backpayments -e SPRING_DATASOURCE_USERNAME=postgres -e SPRING_DATASOURCE_PASSWORD=postgres -p 8080:8080 backpayments:latest

6) Variables de entorno relevantes
- APP_DB_PLATFORM (h2|postgresql) -> selecciona schema-h2.sql o schema-postgresql.sql
- SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD
- APP_SEED_ENABLED, APP_SEED_ADMIN_EMAIL/PASSWORD, APP_SEED_OPERATOR_EMAIL/PASSWORD
- app.security.jwt.secret / expiration

7) Notas y recomendaciones
- Para producción: reemplazar spring.sql.init con Flyway/Liquibase y migrations versionadas.
- Asegurar que el procedimiento de archivado en Postgres tenga la misma semántica transaccional que la prueba en H2.
- Añadir healthcheck y Actuator en Dockerfile si se desea monitoreo.
- Considerar añadir mockito-inline a dependencias de test para evitar advertencias de agente dinámico.

8) Próximos pasos sugeridos (opcionales)
- Añadir pruebas de integración E2E en CI (docker-compose + tests de integración).
- Añadir workflow GitHub Actions para build/test/container push.
- Documentar el contrato de seguridad (cómo obtener JWT para Postman) en docs/AUTH.md.

---
Archivo generado automáticamente por la sesión de desarrollo. Si querés, agrego ejemplo de curl/Postman para el endpoint de archivado y un README de despliegue más detallado.
