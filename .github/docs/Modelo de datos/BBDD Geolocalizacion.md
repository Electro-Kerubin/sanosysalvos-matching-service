# Modelo de Datos - BBDD Geolocalización

## Tabla: mapadecalor
- **id_mapadecalor** (PK)
- geohash
- cantidad_reportes (int)
- last_calculated_at

## Tabla: coordenadas
- **id_ubicacion_coordenadas** (PK)
- ubicacion_lat
- ubicacion_lon
- id_reporte (FK)
- id_comuna (FK)
- direccion
- created_at

## Tabla: comuna
- **id_comuna** (PK)
- nombre_comuna
- id_region (FK)

## Tabla: region
- **id_region** (PK)
- nombre_region

## Relaciones

- coordenadas.id_reporte → reporte_mascota.id_reporte_mascota
- coordenadas.id_comuna → comuna.id_comuna
- comuna.id_region → region.id_region