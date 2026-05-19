package org.sanosysalvos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "reglas_coincidencias")
public class ReglaCoincidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reglas_coincidencias")
    private Long idReglasCoincidencias;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "importancia", nullable = false, precision = 5, scale = 2)
    private BigDecimal importancia;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}

