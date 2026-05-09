# Caso Semestral: Sanos y Salvos

## Plataforma Inteligente para la Localización y Recuperación de Mascotas Perdidas

---

## 1. Contexto

En los últimos años, el aumento en la tenencia de mascotas ha generado una mayor preocupación por su bienestar y seguridad. En muchas ciudades, miles de mascotas se extravían cada año, generando angustia en sus dueños.

Actualmente, la información sobre mascotas perdidas se encuentra dispersa en:
- Redes sociales
- Refugios de animales
- Clínicas veterinarias
- Organizaciones animalistas

### Problemas actuales

- Información desorganizada
- Procesos lentos
- Baja efectividad en la recuperación
- Falta de coordinación entre entidades
- Datos incompletos o desestructurados
- Coincidencias no detectadas a tiempo

---

## 2. La Empresa

**Sanos y Salvos** es una empresa tecnológica enfocada en la localización de mascotas perdidas.

### Colaboradores
- Clínicas veterinarias
- Refugios de animales
- Municipalidades

---

## 3. Propuesta de Solución

Se propone desarrollar una plataforma tecnológica basada en **microservicios** que permita:

- Centralizar la información
- Automatizar la detección de coincidencias
- Facilitar la colaboración entre ciudadanos y organizaciones

---

## 5. Módulos del Sistema

### 5.1 Gestión de Mascotas
Permite registrar mascotas perdidas o encontradas con:
- Características físicas
- Fotografías
- Ubicación geográfica
- Datos de contacto

### 5.2 Sistema de Geolocalización
- Visualización de reportes en mapa
- Identificación de zonas con mayor incidencia

### 5.3 Motor de Coincidencias
Analiza reportes para detectar posibles coincidencias según:
- Raza
- Color
- Tamaño
- Ubicación
- Fecha del reporte

---

## 6. Arquitectura del Sistema

### Enfoque
Arquitectura basada en **microservicios**, orientada a:
- Escalabilidad
- Desacoplamiento
- Modularidad

### Componentes principales

#### API Gateway
- Gestiona la comunicación entre frontend y microservicios

#### Microservicios
- Separación de responsabilidades
- Independencia operativa

---

## 7. Patrones de Diseño

- **Repository Pattern**
    - Manejo de persistencia de datos

- **Factory Method**
    - Creación de instancias

- **Circuit Breaker**
    - Manejo de fallos entre servicios

---

## 8. Requerimientos Técnicos

- Definir microservicios clave
- Asegurar separación de responsabilidades
- Garantizar escalabilidad
- Diseñar API Gateway
- Implementar patrones de diseño
- Mantener servicios desacoplados
- Permitir futuras extensiones sin afectar el sistema
