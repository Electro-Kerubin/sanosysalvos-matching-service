package org.sanosysalvos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReporteDto {

    private Integer idReporteMascota;
    private Integer idTipoReporte;
    private LocalDateTime fechaReporte;
    private LocalDate fechaExtravio;
    private LocalDate fechaAvistamiento;
    private Integer idMascota;
    private String descripcionRaza;
    private String colorPrimario;
    private String colorSecundario;
    private String tamano;

    public Integer getIdReporteMascota() { return idReporteMascota; }
    public void setIdReporteMascota(Integer idReporteMascota) { this.idReporteMascota = idReporteMascota; }

    public Integer getIdTipoReporte() { return idTipoReporte; }
    public void setIdTipoReporte(Integer idTipoReporte) { this.idTipoReporte = idTipoReporte; }

    public LocalDateTime getFechaReporte() { return fechaReporte; }
    public void setFechaReporte(LocalDateTime fechaReporte) { this.fechaReporte = fechaReporte; }

    public LocalDate getFechaExtravio() { return fechaExtravio; }
    public void setFechaExtravio(LocalDate fechaExtravio) { this.fechaExtravio = fechaExtravio; }

    public LocalDate getFechaAvistamiento() { return fechaAvistamiento; }
    public void setFechaAvistamiento(LocalDate fechaAvistamiento) { this.fechaAvistamiento = fechaAvistamiento; }

    public Integer getIdMascota() { return idMascota; }
    public void setIdMascota(Integer idMascota) { this.idMascota = idMascota; }

    public String getDescripcionRaza() { return descripcionRaza; }
    public void setDescripcionRaza(String descripcionRaza) { this.descripcionRaza = descripcionRaza; }

    public String getColorPrimario() { return colorPrimario; }
    public void setColorPrimario(String colorPrimario) { this.colorPrimario = colorPrimario; }

    public String getColorSecundario() { return colorSecundario; }
    public void setColorSecundario(String colorSecundario) { this.colorSecundario = colorSecundario; }

    public String getTamano() { return tamano; }
    public void setTamano(String tamano) { this.tamano = tamano; }

    public LocalDateTime resolveFechaEfectiva() {
        if (fechaReporte != null) return fechaReporte;
        if (fechaExtravio != null) return fechaExtravio.atStartOfDay();
        if (fechaAvistamiento != null) return fechaAvistamiento.atStartOfDay();
        return null;
    }
}
