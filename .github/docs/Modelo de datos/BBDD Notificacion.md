# Modelo de Datos - BBDD Notificación

## Tabla: notificacion
- **id_notificacion** (PK)
- id_canal_preferencia (FK)
- id_reporte (FK)
- id_usuario (FK)
- id_status (FK)
- payload_json
- sent_at (datetime)
- mensaje_error
- created_at (datetime)

## Tabla: notificacion_template
- **id_notificacion_template** (PK)
- id_canal_preferencia (FK)
- asunto
- cuerpo
- is_active

## Tabla: notificacion_envio_intentos
- **id_notificacion_envio_intentos** (PK)
- id_notificacion (FK)
- num_intentos
- response_code
- response_body
- created_at (datetime)
- id_notificacion_status (FK)

## Tabla: notificacion_status
- **id_notificacion_status** (PK)
- descripcion

---

## Relaciones

- notificacion.id_status → notificacion_status.id_notificacion_status

- notificacion_envio_intentos.id_notificacion → notificacion.id_notificacion
- notificacion_envio_intentos.id_notificacion_status → notificacion_status.id_notificacion_status

- notificacion.id_canal_preferencia → canal_preferencia.id_canal_preferencia
- notificacion_template.id_canal_preferencia → canal_preferencia.id_canal_preferencia

- notificacion.id_usuario → usuario.id_usuario
- notificacion.id_reporte → reporte.id_reporte