package org.sanosysalvos.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sanosysalvos.dto.CoincidenciaResultadoResponseDto;
import org.sanosysalvos.dto.CoincidenciaSolicitudResponseDto;
import org.sanosysalvos.service.MatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MatchingController.class)
class MatchingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatchingService matchingService;

    @Test
    void shouldCreateSolicitud() throws Exception {
        CoincidenciaSolicitudResponseDto response = new CoincidenciaSolicitudResponseDto(
                10L,
                1L,
                2L,
                "PENDIENTE",
                LocalDateTime.now(),
                null
        );

        when(matchingService.solicitarCoincidencia(eq(1L), eq(2L))).thenReturn(response);

        mockMvc.perform(post("/api/coincidencias/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"idPerdidoReporte\":1," +
                                "\"idEncontradoReporte\":2" +
                                "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCoincidenciaRequest").value(10));
    }

    @Test
    void shouldListResultsByReport() throws Exception {
        CoincidenciaResultadoResponseDto result = new CoincidenciaResultadoResponseDto(
                99L,
                10L,
                BigDecimal.valueOf(84.5),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(70),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(70),
                BigDecimal.valueOf(70),
                "COINCIDENCIA_ALTA",
                LocalDateTime.now()
        );

        when(matchingService.listarCoincidenciasPorReporte(1L)).thenReturn(List.of(result));

        mockMvc.perform(get("/api/coincidencias/reportes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idCoincidenciaResultado").value(99));
    }
}

