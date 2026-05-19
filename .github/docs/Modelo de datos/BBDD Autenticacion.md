# Modelo de Datos - BBDD Autenticación

## Tabla: refresh_token
- **id_refresh_token** (PK)
- id_usuario (FK)
- token_hash
- expires_at (datetime)
- revoked (boolean)
- created_at (datetime)

## Tabla: usuario
- **id_usuario** (PK)
- email (UK)
- nombre_completo
- id_rol (FK)
- id_status (FK)
- contraseña
- email_verificado (boolean)
- created_at
- updated_at
- last_login_at

## Tabla: usuario_rol
- id_usuario (FK)
- id_role (FK)

## Tabla: rol
- **id_rol** (PK)
- descripcion

## Tabla: status
- **id_status** (PK)
- descripcion

## Relaciones

- refresh_token.id_usuario → usuario.id_usuario
- usuario.id_rol → rol.id_rol
- usuario.id_status → status.id_status
- usuario_rol.id_usuario → usuario.id_usuario
- usuario_rol.id_role → rol.id_rol