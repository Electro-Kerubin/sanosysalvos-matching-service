package org.sanosysalvos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO que representa el mensaje JSON publicado por el microservicio de reportes
 * en la cola RabbitMQ "reporte-coincidencias-queue".
 *
 * Corresponde al ReporteMascotaDTO del reporte-service enriquecido con
 * los campos de la mascota necesarios para el algoritmo de matching.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReporteMascotaEventDto {

    private Integer idReporteMascota;
    private Integer idTipoReporte;
    private String descripcionTipoReporte;
    private Integer idEstatus;
    private String descripcionEstatus;
    private LocalDate fechaExtravio;
    private Integer idContacto;
    private String nombresContacto;
    private LocalDate fechaAvistamiento;
    private LocalDateTime fechaReporte;
    private Integer idMarcaDistintiva;
    private String descripcionMarcaDistintiva;
    private Integer idMascota;
    private String nombreMascota;

    // Campos de la mascota relevantes para el scoring
    private String raza;
    private String colorPrimario;
    private String colorSecundario;
    private String tamano;
    private Double latitud;
    private Double longitud;

    public Integer getIdReporteMascota() { return idReporteMascota; }
    public void setIdReporteMascota(Integer idReporteMascota) { this.idReporteMascota = idReporteMascota; }

    public Integer getIdTipoReporte() { return idTipoReporte; }
    public void setIdTipoReporte(Integer idTipoReporte) { this.idTipoReporte = idTipoReporte; }

    public String getDescripcionTipoReporte() { return descripcionTipoReporte; }
    public void setDescripcionTipoReporte(String descripcionTipoReporte) { this.descripcionTipoReporte = descripcionTipoReporte; }

    public Integer getIdEstatus() { return idEstatus; }
    public void setIdEstatus(Integer idEstatus) { this.idEstatus = idEstatus; }

    public String getDescripcionEstatus() { return descripcionEstatus; }
    public void setDescripcionEstatus(String descripcionEstatus) { this.descripcionEstatus = descripcionEstatus; }

    public LocalDate getFechaExtravio() { return fechaExtravio; }
    public void setFechaExtravio(LocalDate fechaExtravio) { this.fechaExtravio = fechaExtravio; }

    public Integer getIdContacto() { return idContacto; }
    public void setIdContacto(Integer idContacto) { this.idContacto = idContacto; }

    public String getNombresContacto() { return nombresContacto; }
    public void setNombresContacto(String nombresContacto) { this.nombresContacto = nombresContacto; }

    public LocalDate getFechaAvistamiento() { return fechaAvistamiento; }
    public void setFechaAvistamiento(LocalDate fechaAvistamiento) { this.fechaAvistamiento = fechaAvistamiento; }

    public LocalDateTime getFechaReporte() { return fechaReporte; }
    public void setFechaReporte(LocalDateTime fechaReporte) { this.fechaReporte = fechaReporte; }

    public Integer getIdMarcaDistintiva() { return idMarcaDistintiva; }
    public void setIdMarcaDistintiva(Integer idMarcaDistintiva) { this.idMarcaDistintiva = idMarcaDistintiva; }

    public String getDescripcionMarcaDistintiva() { return descripcionMarcaDistintiva; }
    public void setDescripcionMarcaDistintiva(String descripcionMarcaDistintiva) { this.descripcionMarcaDistintiva = descripcionMarcaDistintiva; }

    public Integer getIdMascota() { return idMascota; }
    public void setIdMascota(Integer idMascota) { this.idMascota = idMascota; }

    public String getNombreMascota() { return nombreMascota; }
    public void setNombreMascota(String nombreMascota) { this.nombreMascota = nombreMascota; }

    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }

    public String getColorPrimario() { return colorPrimario; }
    public void setColorPrimario(String colorPrimario) { this.colorPrimario = colorPrimario; }

    public String getColorSecundario() { return colorSecundario; }
    public void setColorSecundario(String colorSecundario) { this.colorSecundario = colorSecundario; }

    public String getTamano() { return tamano; }
    public void setTamano(String tamano) { this.tamano = tamano; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }
}

