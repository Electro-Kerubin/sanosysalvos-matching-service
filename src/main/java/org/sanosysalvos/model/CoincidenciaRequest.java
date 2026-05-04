package org.sanosysalvos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "coincidencia_request")
public class CoincidenciaRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_coincidencia_request")
    private Long idCoincidenciaRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_perdido_reporte", nullable = false)
    private ReporteMascota reportePerdido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_encontrado_reporte", nullable = false)
    private ReporteMascota reporteEncontrado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_coincidencia_status", nullable = false)
    private CoincidenciaStatus status;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}

