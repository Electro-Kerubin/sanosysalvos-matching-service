package org.sanosysalvos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sanosysalvos.model.ReporteMascota;

class ScoringServiceTest {

    private ScoringService scoringService;

    @BeforeEach
    void setUp() {
        scoringService = new ScoringService();
    }

    @Test
    void shouldReturnHighScoreWhenReportsAreVerySimilar() {
        ReporteMascota perdido = baseReporte();
        ReporteMascota encontrado = baseReporte();

        BigDecimal puntajeRaza = scoringService.scoreRaza(perdido, encontrado);
        BigDecimal puntajeColor = scoringService.scoreColor(perdido, encontrado);
        BigDecimal puntajeTamano = scoringService.scoreTamano(perdido, encontrado);
        BigDecimal puntajeDistancia = scoringService.scoreDistancia(perdido, encontrado);
        BigDecimal puntajeFecha = scoringService.scoreFecha(perdido, encontrado);

        BigDecimal total = scoringService.calcularPuntajeTotal(
                puntajeRaza,
                puntajeColor,
                puntajeTamano,
                puntajeDistancia,
                puntajeFecha,
                BigDecimal.valueOf(0.25),
                BigDecimal.valueOf(0.20),
                BigDecimal.valueOf(0.20),
                BigDecimal.valueOf(0.20),
                BigDecimal.valueOf(0.15)
        );

        assertEquals(BigDecimal.valueOf(100.00).setScale(2), total);
        assertEquals("COINCIDENCIA_ALTA", scoringService.veredicto(total));
    }

    @Test
    void shouldReturnLowVerdictWhenNoRelevantFieldsMatch() {
        ReporteMascota perdido = baseReporte();

        ReporteMascota encontrado = new ReporteMascota();
        encontrado.setRaza("poodle");
        encontrado.setColor("blanco");
        encontrado.setTamano("grande");
        encontrado.setLatitud(-33.6000);
        encontrado.setLongitud(-70.8000);
        encontrado.setFechaReporte(LocalDateTime.now().minusDays(20));

        BigDecimal total = scoringService.calcularPuntajeTotal(
                scoringService.scoreRaza(perdido, encontrado),
                scoringService.scoreColor(perdido, encontrado),
                scoringService.scoreTamano(perdido, encontrado),
                scoringService.scoreDistancia(perdido, encontrado),
                scoringService.scoreFecha(perdido, encontrado),
                BigDecimal.valueOf(0.25),
                BigDecimal.valueOf(0.20),
                BigDecimal.valueOf(0.20),
                BigDecimal.valueOf(0.20),
                BigDecimal.valueOf(0.15)
        );

        assertEquals(BigDecimal.valueOf(0.00).setScale(2), total);
        assertEquals("COINCIDENCIA_BAJA", scoringService.veredicto(total));
    }

    private ReporteMascota baseReporte() {
        ReporteMascota reporte = new ReporteMascota();
        reporte.setRaza("labrador");
        reporte.setColor("negro");
        reporte.setTamano("mediano");
        reporte.setLatitud(-33.4489);
        reporte.setLongitud(-70.6693);
        reporte.setFechaReporte(LocalDateTime.now());
        return reporte;
    }
}

