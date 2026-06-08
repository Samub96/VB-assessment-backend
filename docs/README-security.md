# Paso 3 — Seguridad (JWT + RBAC)

Se implementó autenticación basada en JWT y autorización por roles (ADMIN/OPERATOR) con filtros y configuración de endpoints.

## Dependencias clave
- `spring-boot-starter-security`
- `jjwt` (API + impl + Jackson) para emisión y validación de tokens

## Flujo de autenticación
1. El usuario envía credenciales a `POST /api/v1/auth/login`.
2. Se valida con `AuthenticationManager`.
3. Se emite un JWT firmado con HMAC (secret en `application.properties`).
4. El cliente usa `Authorization: Bearer <token>` en cada request.

## Configuración de endpoints
Permisos básicos:
- **Públicos**: `/api/v1/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`.
- **Protegidos**: el resto requiere autenticación.
- **Roles**:
  - `ADMIN` y `OPERATOR` pueden acceder a endpoints base de órdenes (`/api/v1/orders/**`).
  - Las restricciones finas se completarán por endpoint con anotaciones (`@PreAuthorize`) en los controladores.

## Filtro JWT
Se usa `JwtAuthenticationFilter` para:
- Leer el header `Authorization`.
- Validar el token.
- Poblar el `SecurityContext` con el usuario autenticado.

## Datos de prueba (seed)
Se crean usuarios iniciales si `app.seed.enabled=true`:
- **Admin**: `admin@local` / `Admin123!`
- **Operator**: `operator@local` / `Operator123!`

## Propiedades relevantes
```
app.security.jwt.secret=change-me-please-change-me-please-change-me-please
app.security.jwt.expiration=PT2H
app.seed.enabled=true
```
