# Agente: Motor de Coincidencias — Sanos y Salvos

## Descripción General

Microservicio responsable de analizar reportes de mascotas **perdidas** y **encontradas**
para detectar posibles coincidencias de forma automatizada. Aplica un sistema de puntajes
por criterios y un veredicto final para cada par de reportes evaluados.

---

## Funcionalidades

### 1. `solicitarCoincidencia`
Registra una nueva solicitud de análisis entre un reporte de mascota perdida y uno encontrada.

- Crea un registro en `coincidencia_request` con estado inicial (pendiente)
- Asigna `requested_at` con la fecha/hora actual
- Valida que ambos reportes (`id_perdido_reporte`, `id_encontrado_reporte`) existan en `reporte_mascota`

---

### 2. `procesarCoincidencia`
Ejecuta el análisis de coincidencia aplicando las reglas activas definidas en `reglas_coincidencias`.

- Recupera las reglas con `is_active = true`
- Calcula puntajes individuales:
    - `puntaje_raza`
    - `puntaje_color`
    - `puntaje_tamaño`
    - `puntaje_distancia`
    - `puntaje_fecha`
- Calcula `puntaje_total` ponderado según el campo `importancia` de cada regla
- Determina `veredicto_final` en base al puntaje total
- Persiste el resultado en `coincidencias_results`
- Actualiza `processed_at` y el estado en `coincidencia_request`

---

### 3. `obtenerResultadoCoincidencia`
Retorna el resultado de una solicitud de coincidencia ya procesada.

- Busca en `coincidencias_results` por `id_coincidencia_request`
- Retorna puntajes detallados y veredicto final

---

### 4. `listarCoincidenciasPorReporte`
Lista todos los resultados de coincidencia asociados a un reporte específico (perdido o encontrado).

- Filtra `coincidencia_request` por `id_perdido_reporte` o `id_encontrado_reporte`
- Retorna resultados con su puntaje y veredicto

---

### 5. `actualizarEstadoCoincidencia`
Actualiza el estado de una solicitud de coincidencia.

- Modifica `id_coincidencia_status` en `coincidencia_request`
- Estados posibles definidos en catálogo `coincidencia_status` (ej: pendiente, procesado, fallido)

---

### 6. `obtenerReglasActivas`
Retorna todas las reglas activas del motor de coincidencias.

- Filtra `reglas_coincidencias` donde `is_active = true`
- Permite al agente conocer los criterios vigentes antes de procesar

---

### 7. `coincidenciaPotencialEncontrada` ⚡ Circuit Breaker

Notifica o consulta un servicio externo cuando se detecta una coincidencia potencial
(veredicto favorable). Implementa el patrón **Circuit Breaker** para proteger al sistema
ante fallos del servicio destino.

#### Comportamiento del Circuit Breaker

| Estado | Descripción |
|---|---|
| `CLOSED` | Funcionamiento normal, las llamadas se realizan con normalidad |
| `OPEN` | Se detectaron demasiadas fallas. Las llamadas se **bloquean** hasta `next_retry_at` |
| `HALF_OPEN` | Período de prueba: se permite una llamada para verificar si el servicio se recuperó |

#### Flujo

```
coincidenciaPotencialEncontrada(idRequest)
        │
        ▼
¿Estado en circuit_breaker_estado?
        │
   OPEN ──► ¿now >= next_retry_at?
        │         │
        │        NO ──► Retornar error sin llamar al servicio
        │         │
        │        SÍ ──► Pasar a HALF_OPEN
        │
 CLOSED / HALF_OPEN
        │
        ▼
  Llamar servicio externo
        │
   ┌────┴────┐
  ÉXITO    FALLO
   │          │
   ▼          ▼
Incrementar  Incrementar
cantidad_    cantidad_
exitos       fallas
   │          │
   ▼          ▼
Resetear CB  ¿fallas >= limite_fallas?
a CLOSED          │
                 SÍ
                  │
                  ▼
            Pasar a OPEN
            Registrar opened_at
            Calcular next_retry_at
            Guardar last_error
```

#### Persistencia (tabla `circuit_breaker_estado`)

- `estado_circuitbreaker` → FK al catálogo `estado_circuitbreaker` (CLOSED / OPEN / HALF_OPEN)
- `cantidad_fallas` → acumulado de fallas consecutivas
- `cantidad_exitos` → acumulado de éxitos en HALF_OPEN
- `limite_fallas` → umbral configurable para abrir el circuito
- `opened_at` → cuándo se abrió el circuito
- `next_retry_at` → cuándo se permite el próximo reintento
- `last_error` → último error registrado
- `updated_at` → última actualización del estado

---

## Tablas Involucradas

| Tabla | Rol |
|---|---|
| `coincidencia_request` | Registro de solicitudes de análisis |
| `coincidencia_status` | Catálogo de estados de solicitud |
| `coincidencias_results` | Resultados y puntajes del análisis |
| `reglas_coincidencias` | Reglas y pesos del motor |
| `circuit_breaker_estado` | Estado del Circuit Breaker |
| `estado_circuitbreaker` | Catálogo de estados del Circuit Breaker |