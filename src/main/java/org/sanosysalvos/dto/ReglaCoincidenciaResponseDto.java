package org.sanosysalvos.dto;

import java.math.BigDecimal;

public record ReglaCoincidenciaResponseDto(
        Long id,
        String descripcion,
        BigDecimal importancia,
        Boolean activa
) {
}

