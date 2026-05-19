# Modelo de Datos - BBDD Motor de Coincidencias

## Tabla: coincidencia_request
- **id_coincidencia_request** (PK)
- id_perdido_reporte (FK)
- id_encontrado_reporte (FK)
- id_coincidencia_status (FK)
- requested_at (datetime)
- processed_at (datetime)

## Tabla: coincidencia_status
- **id_coincidencia_status** (PK)
- descripcion

## Tabla: coincidencias_results
- **id_coincidencia_resultado** (PK)
- id_coincidencia_request (FK)
- puntaje_total
- puntaje_raza
- puntaje_color
- puntaje_tamaño
- puntaje_distancia
- puntaje_fecha
- veredicto_final
- created_at

## Tabla: reglas_coincidencias
- **id_reglas_coincidencias** (PK)
- descripcion
- importancia (decimal)
- is_active

## Tabla: circuit_breaker_estado
- **id_circuit_breaker_estado** (PK)
- estado_circuitbreaker (FK)
- cantidad_fallas
- cantidad_exitos
- limite_fallas
- opened_at
- next_retry_at
- last_error
- updated_at

## Tabla: estado_circuitbreaker
- **id_estado_circuitbreaker** (PK)
- descripcion

## Relaciones

- coincidencia_request.id_coincidencia_status → coincidencia_status.id_coincidencia_status
- coincidencia_request.id_perdido_reporte → reporte_mascota.id_reporte_mascota
- coincidencia_request.id_encontrado_reporte → reporte_mascota.id_reporte_mascota

- coincidencias_results.id_coincidencia_request → coincidencia_request.id_coincidencia_request

- circuit_breaker_estado.estado_circuitbreaker → estado_circuitbreaker.id_estado_circuitbreaker