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
@Table(name = "circuit_breaker_estado")
public class CircuitBreakerEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_circuit_breaker_estado")
    private Long idCircuitBreakerEstado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_circuitbreaker", nullable = false)
    private EstadoCircuitBreaker estadoCircuitbreaker;

    @Column(name = "cantidad_fallas", nullable = false)
    private Integer cantidadFallas;

    @Column(name = "cantidad_exitos", nullable = false)
    private Integer cantidadExitos;

    @Column(name = "limite_fallas", nullable = false)
    private Integer limiteFallas;

    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

