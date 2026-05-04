package org.sanosysalvos.dto;

public record NotificacionResultadoDto(
        Long idCoincidenciaRequest,
        String estadoCircuitBreaker,
        String mensaje
) {
}

