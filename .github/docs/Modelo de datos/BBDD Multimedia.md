# Modelo de Datos - BBDD multimedia

## Tabla: multimedia
- **id_multimedia** (PK)
- id_reporte_mascota (FK)
- id_tipo_multimedia (FK)
- s3_bucket
- s3_key
- size_bytes
- hash
- id_usuario (FK)
- created_at (datetime)

## Tabla: multimedia_info
- **id_multimedia_info** (PK)
- id_multimedia (FK)
- width
- height
- created_at (datetime)

---

## Relaciones

- multimedia_info.id_multimedia → multimedia.id_multimedia

- multimedia.id_reporte_mascota → reporte_mascota.id_reporte_mascota
- multimedia.id_tipo_multimedia → tipo_multimedia.id_tipo_multimedia
- multimedia.id_usuario → usuario.id_usuario