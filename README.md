# sanosysalvos-matching-service

Microservicio Spring Boot para el motor de coincidencias de reportes de mascotas perdidas y encontradas.

## Funcionalidades implementadas

- Crear solicitud de coincidencia (`coincidencia_request`)
- Procesar coincidencia con reglas activas (`reglas_coincidencias`)
- Obtener resultado por solicitud (`coincidencias_results`)
- Listar coincidencias por reporte de mascota
- Actualizar estado de solicitud (`coincidencia_status`)
- Consultar reglas activas
- Notificar coincidencia potencial protegida por Circuit Breaker (`circuit_breaker_estado`)

## Endpoints

- `POST /api/coincidencias/solicitudes`
- `POST /api/coincidencias/solicitudes/{idCoincidenciaRequest}/procesar`
- `GET /api/coincidencias/solicitudes/{idCoincidenciaRequest}/resultado`
- `GET /api/coincidencias/reportes/{idReporteMascota}`
- `PATCH /api/coincidencias/solicitudes/{idCoincidenciaRequest}/estado`
- `GET /api/coincidencias/reglas/activas`
- `POST /api/coincidencias/solicitudes/{idCoincidenciaRequest}/notificar-potencial`

## Variables de configuracion

- `MATCHING_DB_URL` (default: `jdbc:postgresql://localhost:5432/matching_db`)
- `MATCHING_DB_USER` (default: `postgres`)
- `MATCHING_DB_PASSWORD` (default: `postgres`)
- `MATCHING_NOTIFICATION_URL` (opcional, endpoint externo para notificaciones)
- `MATCHING_CB_FAILURE_THRESHOLD` (default: `3`)
- `MATCHING_CB_RETRY_SECONDS` (default: `60`)

## Ejecutar pruebas

```powershell
mvn test
```

## Levantar servicio

```powershell
mvn spring-boot:run
```

