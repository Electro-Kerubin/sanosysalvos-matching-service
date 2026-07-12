package org.sanosysalvos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sanosysalvos.model.ReporteMascota;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ScoringServiceTest {

    private ScoringService scoringService;
    private ReporteMascota reportePerdido;
    private ReporteMascota reporteAvistamiento;

    @BeforeEach
    void setUp() {
        scoringService = new ScoringService();

        reportePerdido = new ReporteMascota();
        reportePerdido.setRaza("Labrador");
        reportePerdido.setColor("negro");
        reportePerdido.setTamano("mediano");
        reportePerdido.setLatitud(-33.045);
        reportePerdido.setLongitud(-71.610);
        reportePerdido.setFechaReporte(LocalDateTime.now().minusDays(1));

        reporteAvistamiento = new ReporteMascota();
        reporteAvistamiento.setRaza("Labrador");
        reporteAvistamiento.setColor("negro");
        reporteAvistamiento.setTamano("mediano");
        reporteAvistamiento.setLatitud(-33.046);
        reporteAvistamiento.setLongitud(-71.611);
        reporteAvistamiento.setFechaReporte(LocalDateTime.now());
    }

    @Test
    void scoreRaza_debeRetornar100_cuandoRazasIguales() {
        BigDecimal score = scoringService.scoreRaza(reportePerdido, reporteAvistamiento);
        assertEquals(new BigDecimal("100"), score);
    }

    @Test
    void scoreRaza_debeRetornar0_cuandoRazasDiferentes() {
        reporteAvistamiento.setRaza("Pastor Alemán");
        BigDecimal score = scoringService.scoreRaza(reportePerdido, reporteAvistamiento);
        assertEquals(BigDecimal.ZERO, score);
    }

    @Test
    void scoreRaza_debeRetornar0_cuandoRazaEsNula() {
        reporteAvistamiento.setRaza(null);
        BigDecimal score = scoringService.scoreRaza(reportePerdido, reporteAvistamiento);
        assertEquals(BigDecimal.ZERO, score);
    }

    @Test
    void scoreColor_debeRetornar100_cuandoColoresIguales() {
        BigDecimal score = scoringService.scoreColor(reportePerdido, reporteAvistamiento);
        assertEquals(new BigDecimal("100"), score);
    }

    @Test
    void scoreColor_debeRetornar0_cuandoColoresDiferentes() {
        reporteAvistamiento.setColor("blanco");
        BigDecimal score = scoringService.scoreColor(reportePerdido, reporteAvistamiento);
        assertEquals(BigDecimal.ZERO, score);
    }

    @Test
    void scoreTamano_debeRetornar100_cuandoTamanosIguales() {
        BigDecimal score = scoringService.scoreTamano(reportePerdido, reporteAvistamiento);
        assertEquals(new BigDecimal("100"), score);
    }

    @Test
    void scoreTamano_debeRetornar0_cuandoTamanosDiferentes() {
        reporteAvistamiento.setTamano("grande");
        BigDecimal score = scoringService.scoreTamano(reportePerdido, reporteAvistamiento);
        assertEquals(BigDecimal.ZERO, score);
    }

    @Test
    void scoreDistancia_debeRetornar100_cuandoDistanciaEsMenorA2km() {
        BigDecimal score = scoringService.scoreDistancia(reportePerdido, reporteAvistamiento);
        assertEquals(new BigDecimal("100"), score);
    }

    @Test
    void scoreDistancia_debeRetornar0_cuandoCoordenadaEsNula() {
        reporteAvistamiento.setLatitud(null);
        BigDecimal score = scoringService.scoreDistancia(reportePerdido, reporteAvistamiento);
        assertEquals(BigDecimal.ZERO, score);
    }

    @Test
    void scoreFecha_debeRetornar100_cuandoFechaDiferenciaEsMenorA1Dia() {
        BigDecimal score = scoringService.scoreFecha(reportePerdido, reporteAvistamiento);
        assertEquals(new BigDecimal("100"), score);
    }

    @Test
    void scoreFecha_debeRetornar0_cuandoFechaEsNula() {
        reporteAvistamiento.setFechaReporte(null);
        BigDecimal score = scoringService.scoreFecha(reportePerdido, reporteAvistamiento);
        assertEquals(BigDecimal.ZERO, score);
    }

    @Test
    void veredicto_debeRetornarCoincidenciaAlta_cuandoPuntajeMayorA75() {
        String veredicto = scoringService.veredicto(new BigDecimal("80"));
        assertEquals("COINCIDENCIA_ALTA", veredicto);
    }

    @Test
    void veredicto_debeRetornarCoincidenciaMedia_cuandoPuntajeEntre50Y75() {
        String veredicto = scoringService.veredicto(new BigDecimal("60"));
        assertEquals("COINCIDENCIA_MEDIA", veredicto);
    }

    @Test
    void veredicto_debeRetornarCoincidenciaBaja_cuandoPuntajeMenorA50() {
        String veredicto = scoringService.veredicto(new BigDecimal("30"));
        assertEquals("COINCIDENCIA_BAJA", veredicto);
    }
}