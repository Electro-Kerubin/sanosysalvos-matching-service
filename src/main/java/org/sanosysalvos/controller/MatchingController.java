package org.sanosysalvos.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.sanosysalvos.dto.ActualizarEstadoRequestDto;
import org.sanosysalvos.dto.CoincidenciaResultadoResponseDto;
import org.sanosysalvos.dto.CoincidenciaSolicitudResponseDto;
import org.sanosysalvos.dto.CrearCoincidenciaRequestDto;
import org.sanosysalvos.dto.NotificacionResultadoDto;
import org.sanosysalvos.dto.ReglaCoincidenciaResponseDto;
import org.sanosysalvos.service.MatchingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coincidencias")
public class MatchingController {

    private final MatchingService matchingService;

    public MatchingController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @PostMapping("/solicitudes")
    public ResponseEntity<CoincidenciaSolicitudResponseDto> solicitarCoincidencia(
            @Valid @RequestBody CrearCoincidenciaRequestDto request
    ) {
        CoincidenciaSolicitudResponseDto response = matchingService.solicitarCoincidencia(
                request.idPerdidoReporte(),
                request.idEncontradoReporte()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/solicitudes/{idCoincidenciaRequest}/procesar")
    public ResponseEntity<CoincidenciaResultadoResponseDto> procesarCoincidencia(
            @PathVariable Long idCoincidenciaRequest
    ) {
        return ResponseEntity.ok(matchingService.procesarCoincidencia(idCoincidenciaRequest));
    }

    @GetMapping("/solicitudes/{idCoincidenciaRequest}/resultado")
    public ResponseEntity<CoincidenciaResultadoResponseDto> obtenerResultadoCoincidencia(
            @PathVariable Long idCoincidenciaRequest
    ) {
        return ResponseEntity.ok(matchingService.obtenerResultadoCoincidencia(idCoincidenciaRequest));
    }

    @GetMapping("/reportes/{idReporteMascota}")
    public ResponseEntity<List<CoincidenciaResultadoResponseDto>> listarCoincidenciasPorReporte(
            @PathVariable Long idReporteMascota
    ) {
        return ResponseEntity.ok(matchingService.listarCoincidenciasPorReporte(idReporteMascota));
    }

    @PatchMapping("/solicitudes/{idCoincidenciaRequest}/estado")
    public ResponseEntity<CoincidenciaSolicitudResponseDto> actualizarEstadoCoincidencia(
            @PathVariable Long idCoincidenciaRequest,
            @Valid @RequestBody ActualizarEstadoRequestDto request
    ) {
        return ResponseEntity.ok(matchingService.actualizarEstadoCoincidencia(idCoincidenciaRequest, request.estado()));
    }

    @GetMapping("/reglas/activas")
    public ResponseEntity<List<ReglaCoincidenciaResponseDto>> obtenerReglasActivas() {
        return ResponseEntity.ok(matchingService.obtenerReglasActivas());
    }

    @PostMapping("/solicitudes/{idCoincidenciaRequest}/notificar-potencial")
    public ResponseEntity<NotificacionResultadoDto> notificarCoincidenciaPotencial(
            @PathVariable Long idCoincidenciaRequest
    ) {
        return ResponseEntity.ok(matchingService.coincidenciaPotencialEncontrada(idCoincidenciaRequest));
    }
}

