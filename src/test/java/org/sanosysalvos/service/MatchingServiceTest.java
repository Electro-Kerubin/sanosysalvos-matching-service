package org.sanosysalvos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sanosysalvos.dto.CoincidenciaResultadoResponseDto;
import org.sanosysalvos.dto.CoincidenciaSolicitudResponseDto;
import org.sanosysalvos.exception.BusinessException;
import org.sanosysalvos.exception.NotFoundException;
import org.sanosysalvos.model.*;
import org.sanosysalvos.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    @Mock private CoincidenciaRequestRepository coincidenciaRequestRepository;
    @Mock private CoincidenciaResultRepository coincidenciaResultRepository;
    @Mock private CoincidenciaStatusRepository coincidenciaStatusRepository;
    @Mock private ReglaCoincidenciaRepository reglaCoincidenciaRepository;
    @Mock private ReporteMascotaRepository reporteMascotaRepository;
    @Mock private ScoringService scoringService;
    @Mock private CircuitBreakerService circuitBreakerService;
    @Mock private CoincidenciaNotifier coincidenciaNotifier;

    @InjectMocks private MatchingService matchingService;

    private ReporteMascota reportePerdido;
    private ReporteMascota reporteAvistamiento;
    private CoincidenciaStatus statusPendiente;
    private CoincidenciaStatus statusProcesado;

    @BeforeEach
    void setUp() {
        reportePerdido = new ReporteMascota();
        reportePerdido.setIdReporteMascota(1L);
        reportePerdido.setIdTipoReporte(1);
        reportePerdido.setRaza("Labrador");
        reportePerdido.setColor("negro");
        reportePerdido.setTamano("mediano");
        reportePerdido.setLatitud(-33.045);
        reportePerdido.setLongitud(-71.610);

        reporteAvistamiento = new ReporteMascota();
        reporteAvistamiento.setIdReporteMascota(2L);
        reporteAvistamiento.setIdTipoReporte(3);
        reporteAvistamiento.setRaza("Labrador");
        reporteAvistamiento.setColor("negro");
        reporteAvistamiento.setTamano("mediano");
        reporteAvistamiento.setLatitud(-33.046);
        reporteAvistamiento.setLongitud(-71.611);

        statusPendiente = new CoincidenciaStatus();
        statusPendiente.setDescripcion("PENDIENTE");

        statusProcesado = new CoincidenciaStatus();
        statusProcesado.setDescripcion("PROCESADO");
    }

    @Test
    void syncCoincidencias_debeRetornarListaVacia_cuandoTipoEsEncontrada() {
        ReporteMascota reporteEncontrado = new ReporteMascota();
        reporteEncontrado.setIdReporteMascota(3L);
        reporteEncontrado.setIdTipoReporte(2); // Encontrada

        when(reporteMascotaRepository.findById(3L)).thenReturn(Optional.of(reporteEncontrado));

        List<CoincidenciaResultadoResponseDto> result = matchingService.syncCoincidencias(3L);

        assertTrue(result.isEmpty());
        verify(reporteMascotaRepository, never()).findByIdTipoReporte(anyInt());
    }

    @Test
    void syncCoincidencias_debeRetornarListaVacia_cuandoNoHayReportesContrarios() {
        when(reporteMascotaRepository.findById(1L)).thenReturn(Optional.of(reportePerdido));
        when(reporteMascotaRepository.findByIdTipoReporte(3)).thenReturn(List.of());

        List<CoincidenciaResultadoResponseDto> result = matchingService.syncCoincidencias(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void syncCoincidencias_debeLanzarNotFoundException_cuandoReporteNoExiste() {
        when(reporteMascotaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> matchingService.syncCoincidencias(99L));
    }

    @Test
    void solicitarCoincidencia_debeCrearSolicitudCorrectamente() {
        when(reporteMascotaRepository.findById(1L)).thenReturn(Optional.of(reportePerdido));
        when(reporteMascotaRepository.findById(2L)).thenReturn(Optional.of(reporteAvistamiento));
        when(coincidenciaStatusRepository.findByDescripcion("PENDIENTE")).thenReturn(Optional.of(statusPendiente));

        CoincidenciaRequest savedRequest = new CoincidenciaRequest();
        savedRequest.setIdCoincidenciaRequest(10L);
        savedRequest.setReportePerdido(reportePerdido);
        savedRequest.setReporteEncontrado(reporteAvistamiento);
        savedRequest.setStatus(statusPendiente);
        savedRequest.setRequestedAt(LocalDateTime.now());

        when(coincidenciaRequestRepository.save(any())).thenReturn(savedRequest);

        CoincidenciaSolicitudResponseDto result = matchingService.solicitarCoincidencia(1L, 2L);

        assertNotNull(result);
        assertEquals(10L, result.idCoincidenciaRequest());
        assertEquals(1L, result.idPerdidoReporte());
        assertEquals(2L, result.idEncontradoReporte());
        assertEquals("PENDIENTE", result.estado());
    }

    @Test
    void solicitarCoincidencia_debeLanzarNotFoundException_cuandoReportePerdidoNoExiste() {
        when(reporteMascotaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> matchingService.solicitarCoincidencia(99L, 2L));
    }

    @Test
    void procesarCoincidencia_debeCalcularPuntajeYGuardarResultado() {
        CoincidenciaRequest request = new CoincidenciaRequest();
        request.setIdCoincidenciaRequest(10L);
        request.setReportePerdido(reportePerdido);
        request.setReporteEncontrado(reporteAvistamiento);
        request.setStatus(statusPendiente);

        ReglaCoincidencia reglaRaza = new ReglaCoincidencia();
        reglaRaza.setDescripcion("raza");
        reglaRaza.setImportancia(new BigDecimal("0.25"));
        reglaRaza.setIsActive(true);

        ReglaCoincidencia reglaColor = new ReglaCoincidencia();
        reglaColor.setDescripcion("color");
        reglaColor.setImportancia(new BigDecimal("0.20"));
        reglaColor.setIsActive(true);

        ReglaCoincidencia reglaTamano = new ReglaCoincidencia();
        reglaTamano.setDescripcion("tamano");
        reglaTamano.setImportancia(new BigDecimal("0.20"));
        reglaTamano.setIsActive(true);

        ReglaCoincidencia reglaDistancia = new ReglaCoincidencia();
        reglaDistancia.setDescripcion("distancia");
        reglaDistancia.setImportancia(new BigDecimal("0.20"));
        reglaDistancia.setIsActive(true);

        ReglaCoincidencia reglaFecha = new ReglaCoincidencia();
        reglaFecha.setDescripcion("fecha");
        reglaFecha.setImportancia(new BigDecimal("0.15"));
        reglaFecha.setIsActive(true);

        when(coincidenciaRequestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(coincidenciaResultRepository.findByCoincidenciaRequest_IdCoincidenciaRequest(10L)).thenReturn(Optional.empty());
        when(reglaCoincidenciaRepository.findByIsActiveTrue()).thenReturn(List.of(reglaRaza, reglaColor, reglaTamano, reglaDistancia, reglaFecha));
        when(scoringService.scoreRaza(any(), any())).thenReturn(new BigDecimal("100"));
        when(scoringService.scoreColor(any(), any())).thenReturn(new BigDecimal("100"));
        when(scoringService.scoreTamano(any(), any())).thenReturn(new BigDecimal("100"));
        when(scoringService.scoreDistancia(any(), any())).thenReturn(new BigDecimal("100"));
        when(scoringService.scoreFecha(any(), any())).thenReturn(new BigDecimal("100"));
        when(scoringService.calcularPuntajeTotal(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(new BigDecimal("100"));
        when(scoringService.veredicto(any())).thenReturn("COINCIDENCIA_ALTA");
        when(coincidenciaStatusRepository.findByDescripcion("PROCESADO")).thenReturn(Optional.of(statusProcesado));

        CoincidenciaResult savedResult = new CoincidenciaResult();
        savedResult.setIdCoincidenciaResultado(1L);
        savedResult.setCoincidenciaRequest(request);
        savedResult.setPuntajeRaza(new BigDecimal("100"));
        savedResult.setPuntajeColor(new BigDecimal("100"));
        savedResult.setPuntajeTamano(new BigDecimal("100"));
        savedResult.setPuntajeDistancia(new BigDecimal("100"));
        savedResult.setPuntajeFecha(new BigDecimal("100"));
        savedResult.setPuntajeTotal(new BigDecimal("100"));
        savedResult.setVeredictoFinal("COINCIDENCIA_ALTA");
        savedResult.setCreatedAt(LocalDateTime.now());

        when(coincidenciaResultRepository.save(any())).thenReturn(savedResult);
        when(coincidenciaRequestRepository.save(any())).thenReturn(request);

        CoincidenciaResultadoResponseDto result = matchingService.procesarCoincidencia(10L);

        assertNotNull(result);
        assertEquals("COINCIDENCIA_ALTA", result.veredictoFinal());
        assertEquals(new BigDecimal("100"), result.puntajeTotal());
    }

    @Test
    void procesarCoincidencia_debeLanzarBusinessException_cuandoYaFueProcesada() {
        CoincidenciaRequest request = new CoincidenciaRequest();
        request.setIdCoincidenciaRequest(10L);
        request.setReportePerdido(reportePerdido);
        request.setReporteEncontrado(reporteAvistamiento);

        when(coincidenciaRequestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(coincidenciaResultRepository.findByCoincidenciaRequest_IdCoincidenciaRequest(10L))
            .thenReturn(Optional.of(new CoincidenciaResult()));

        assertThrows(BusinessException.class, () -> matchingService.procesarCoincidencia(10L));
    }

    @Test
    void obtenerReglasActivas_debeRetornarTodasLasReglasActivas() {
        ReglaCoincidencia regla = new ReglaCoincidencia();
        regla.setIdReglasCoincidencias(1L);
        regla.setDescripcion("raza");
        regla.setImportancia(new BigDecimal("0.25"));
        regla.setIsActive(true);

        when(reglaCoincidenciaRepository.findByIsActiveTrue()).thenReturn(List.of(regla));

        var result = matchingService.obtenerReglasActivas();

        assertEquals(1, result.size());
        assertEquals("raza", result.get(0).descripcion());
    }
}