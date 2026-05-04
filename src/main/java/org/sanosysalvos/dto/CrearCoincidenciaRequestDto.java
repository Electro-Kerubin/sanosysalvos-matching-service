package org.sanosysalvos.dto;

import jakarta.validation.constraints.NotNull;

public record CrearCoincidenciaRequestDto(
        @NotNull(message = "idPerdidoReporte es obligatorio") Long idPerdidoReporte,
        @NotNull(message = "idEncontradoReporte es obligatorio") Long idEncontradoReporte
) {
}

