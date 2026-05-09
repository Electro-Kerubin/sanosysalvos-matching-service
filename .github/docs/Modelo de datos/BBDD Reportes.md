# Modelo de Datos - BBDD Reportes

## Tabla: tipo_reporte
- **id_tipo_reporte** (PK)
- descripcion_tipo_reporte

## Tabla: estatus
- **id_estatus** (PK)
- descripcion_estatus

## Tabla: contacto
- **id_contacto** (PK)
- nombres (varchar)
- correo (varchar)
- telefono (number)
- id_canal_preferencia (FK)

## Tabla: canal_preferencia
- **id_canal_preferencia** (PK)
- descripcion

## Tabla: reporte_mascota
- **id_reporte_mascota** (PK)
- id_tipo_reporte (FK)
- id_estatus (FK)
- fecha_extravio (DATE)
- id_contacto (FK)
- fecha_avistamiento (DATE)
- fecha_reporte (timestamp)
- id_marca_distintiva (FK)
- id_mascota (FK)

## Tabla: mascota
- **id_mascota** (PK)
- nombre_mascota
- id_raza (FK)
- id_especie (FK)
- color_primario (varchar)
- color_secundario (varchar)
- tamano (varchar)
- id_sexo (FK)
- edad (number)
- detalles_extra (varchar)
- id_chip (FK, opcional)

## Tabla: raza
- **id_raza** (PK)
- descripcion

## Tabla: especie
- **id_especie** (PK)
- descripcion

## Tabla: sexo
- **id_sexo** (PK)
- descripcion

## Tabla: marca_distintiva
- **id_marca_distintiva** (PK)
- descripcion

## Relaciones

- reporte_mascota.id_tipo_reporte → tipo_reporte.id_tipo_reporte
- reporte_mascota.id_estatus → estatus.id_estatus
- reporte_mascota.id_contacto → contacto.id_contacto
- reporte_mascota.id_mascota → mascota.id_mascota
- reporte_mascota.id_marca_distintiva → marca_distintiva.id_marca_distintiva

- mascota.id_raza → raza.id_raza
- mascota.id_especie → especie.id_especie
- mascota.id_sexo → sexo.id_sexo

- contacto.id_canal_preferencia → canal_preferencia.id_canal_preferencia