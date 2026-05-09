package org.sanosysalvos.client;

import org.sanosysalvos.dto.CoordenadaResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Cliente Feign para el microservicio de geolocalización.
 * Obtiene las coordenadas (lat/lon) asociadas a un reporte de mascota.
 *
 * Endpoint consumido: GET /api/coordenadas/reporte/{idReporte}
 */
@FeignClient(name = "geolocation-service", url = "${geolocation.service.url}")
public interface GeolocationClient {

    @GetMapping("/api/coordenadas/reporte/{idReporte}")
    CoordenadaResponseDto getCoordenadaByReporte(@PathVariable("idReporte") Long idReporte);
}

