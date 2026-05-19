package org.sanosysalvos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CoincidenciaResultadoResponseDto(
        Long idCoincidenciaResultado,
        Long idCoincidenciaRequest,
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

