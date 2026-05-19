package org.sanosysalvos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "reporte_mascota")
public class ReporteMascota {

    @Id
    @Column(name = "id_reporte_mascota")
    private Long idReporteMascota;

    @Column(name = "raza")
    private String raza;

    @Column(name = "color")
    private String color;

    @Column(name = "tamano")
    private String tamano;

    @Column(name = "id_tipo_reporte")
    private Integer idTipoReporte;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    @Column(name = "fecha_reporte")
    private LocalDateTime fechaReporte;
}

