package org.sanosysalvos.dto;

import java.time.LocalDateTime;

public record CoincidenciaSolicitudResponseDto(
        Long idCoincidenciaRequest,
        Long idPerdidoReporte,
        Long idEncontradoReporte,
        String estado,
        LocalDateTime requestedAt,
        LocalDateTime processedAt
) {
}

