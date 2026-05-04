package org.sanosysalvos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "coincidencias_results")
public class CoincidenciaResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_coincidencia_resultado")
    private Long idCoincidenciaResultado;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_coincidencia_request", nullable = false, unique = true)
    private CoincidenciaRequest coincidenciaRequest;

    @Column(name = "puntaje_total", nullable = false, precision = 5, scale = 2)
    private BigDecimal puntajeTotal;

    @Column(name = "puntaje_raza", nullable = false, precision = 5, scale = 2)
    private BigDecimal puntajeRaza;

    @Column(name = "puntaje_color", nullable = false, precision = 5, scale = 2)
    private BigDecimal puntajeColor;

    @Column(name = "puntaje_tamano", nullable = false, precision = 5, scale = 2)
    private BigDecimal puntajeTamano;

    @Column(name = "puntaje_distancia", nullable = false, precision = 5, scale = 2)
    private BigDecimal puntajeDistancia;

    @Column(name = "puntaje_fecha", nullable = false, precision = 5, scale = 2)
    private BigDecimal puntajeFecha;

    @Column(name = "veredicto_final", nullable = false)
    private String veredictoFinal;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}

