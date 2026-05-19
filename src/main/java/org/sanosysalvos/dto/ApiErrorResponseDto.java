package org.sanosysalvos.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponseDto(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        List<String> details
) {
}

