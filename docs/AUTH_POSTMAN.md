Autenticación y ejemplos (curl / Postman)

1) Obtener JWT (login)

Endpoint:
POST http://localhost:8080/api/v1/auth/login

Payload JSON de ejemplo (admin seed por defecto):
{
  "email": "admin@local",
  "password": "Admin123!"
}

Curl (dev):

curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@local","password":"Admin123!"}' | jq -r .token

- La respuesta es un JSON: { "token": "<jwt>", "tokenType": "Bearer", "expiresAt": "...", "roles": ["ROLE_ADMIN"] }
- Para obtener solo el token con jq usa: jq -r .token

2) Llamar al endpoint administrativo (archive)

Header requerido: Authorization: Bearer <token>

Curl ejemplo (invoca archivado):

TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login -H 'Content-Type: application/json' -d '{"email":"admin@local","password":"Admin123!"}' | jq -r .token)

curl -i -X POST http://localhost:8080/api/v1/orders/archive-rejected \
  -H "Authorization: Bearer $TOKEN"

- Respuesta esperada: 204 No Content si el usuario tiene rol ADMIN.

3) Uso en Postman

- Crear request POST http://localhost:8080/api/v1/auth/login con body JSON y obtener token.
- En la request administrativa, ir a "Authorization" -> "Type" = Bearer Token -> pegar el token.
- Alternativamente añadir header Authorization: Bearer <token> en la pestaña "Headers".

4) Notas

- Valores por defecto del seed (si está activo):
  - admin: admin@local / Admin123!
  - operator: operator@local / Operator123!
- Si la app está en otra URL/puerto, ajustar la base URL.
- Si Actuator está habilitado, comprobar /actuator/health para validar que el servicio está listo.
