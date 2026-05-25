package org.sanosysalvos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReporteDto {

    private Integer idReporteMascota;
    private Integer idTipoReporte;
    private LocalDateTime fechaReporte;
    private Integer idMascota;

    public Integer getIdReporteMascota() { return idReporteMascota; }
    public void setIdReporteMascota(Integer idReporteMascota) { this.idReporteMascota = idReporteMascota; }

    public Integer getIdTipoReporte() { return idTipoReporte; }
    public void setIdTipoReporte(Integer idTipoReporte) { this.idTipoReporte = idTipoReporte; }

    public LocalDateTime getFechaReporte() { return fechaReporte; }
    public void setFechaReporte(LocalDateTime fechaReporte) { this.fechaReporte = fechaReporte; }

    public Integer getIdMascota() { return idMascota; }
    public void setIdMascota(Integer idMascota) { this.idMascota = idMascota; }
}
