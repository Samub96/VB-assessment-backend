# Paso 2 — Bootstrap del backend

Se preparó la base del backend con Spring Boot + Spring Cloud y una estructura de paquetes coherente con el diseño por capas. Esto permite avanzar rápidamente con servicios, controladores y seguridad sin rehacer la base.

## Dependencias agregadas (pom.xml)
- **Spring Web** para exponer APIs REST.
- **Spring Data JPA** para persistencia.
- **Validation** para validaciones de DTOs.
- **Spring Cloud OpenFeign** para integración externa.
- **Spring Cloud Circuit Breaker (Resilience4j)** para timeouts y reintentos simples.
- **PostgreSQL** y **H2** como drivers de BD (runtime).

## Estructura de paquetes
```
com.assesment.backpayments
├── application
│   ├── dto
│   └── mapper
└── domain
    └── model
```

## Entidades de dominio
- `Order`
- `OrderStatusLog`
- `User`
- `Role`
- `OrderStatus` (enum)
- `RoleName` (enum)

## DTOs
- `CreateOrderRequest`
- `OrderResponse`
- `OrderSummaryResponse`
- `UserResponse`

## Mappers
- `OrderMapper`
- `UserMapper`

## Spring Cloud habilitado
Se activó `@EnableFeignClients` en `BackPaymentsApplication` para dejar lista la integración externa vía Feign.
