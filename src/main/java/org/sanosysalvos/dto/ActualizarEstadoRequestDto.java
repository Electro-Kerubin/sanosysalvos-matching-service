package org.sanosysalvos.dto;

import jakarta.validation.constraints.NotBlank;

public record ActualizarEstadoRequestDto(
        @NotBlank(message = "estado es obligatorio") String estado
) {
}

