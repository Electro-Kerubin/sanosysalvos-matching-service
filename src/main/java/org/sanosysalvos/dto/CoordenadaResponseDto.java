package org.sanosysalvos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO que mapea la respuesta del geolocation-service para el endpoint
 * GET /api/coordenadas/reporte/{idReporte}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoordenadaResponseDto {

    private Long idUbicacionCoordenadas;
    private Double ubicacionLat;
    private Double ubicacionLon;
    private Long idReporte;
    private String direccion;

    public Long getIdUbicacionCoordenadas() { return idUbicacionCoordenadas; }
    public void setIdUbicacionCoordenadas(Long idUbicacionCoordenadas) { this.idUbicacionCoordenadas = idUbicacionCoordenadas; }

    public Double getUbicacionLat() { return ubicacionLat; }
    public void setUbicacionLat(Double ubicacionLat) { this.ubicacionLat = ubicacionLat; }

    public Double getUbicacionLon() { return ubicacionLon; }
    public void setUbicacionLon(Double ubicacionLon) { this.ubicacionLon = ubicacionLon; }

    public Long getIdReporte() { return idReporte; }
    public void setIdReporte(Long idReporte) { this.idReporte = idReporte; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}

