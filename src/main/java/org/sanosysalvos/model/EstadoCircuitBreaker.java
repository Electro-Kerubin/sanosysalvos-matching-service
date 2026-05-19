package org.sanosysalvos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "estado_circuitbreaker")
public class EstadoCircuitBreaker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_circuitbreaker")
    private Long idEstadoCircuitbreaker;

    @Column(name = "descripcion", nullable = false, unique = true)
    private String descripcion;
}

