package org.sanosysalvos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CoincidenciaConReporteDto(
        Long idCoincidenciaResultado,
        Long idCoincidenciaRequest,
        Long idReportePerdido,
        Long idReporteEncontrado,
        BigDecimal puntajeTotal,
        BigDecimal puntajeRaza,
        BigDecimal puntajeColor,
        BigDecimal puntajeTamano,
        BigDecimal puntajeDistancia,
        BigDecimal puntajeFecha,
        String veredictoFinal,
        LocalDateTime createdAt
) {
}
