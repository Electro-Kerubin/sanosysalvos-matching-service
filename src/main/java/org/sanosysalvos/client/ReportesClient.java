package org.sanosysalvos.client;

import java.util.List;
import org.sanosysalvos.dto.MascotaDto;
import org.sanosysalvos.dto.ReporteDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "reportes-client", url = "${reportes.service.url}")
public interface ReportesClient {

    @GetMapping("/api/reportes")
    List<ReporteDto> getAllReportes();

    @GetMapping("/api/reportes/{id}")
    ReporteDto getReporte(@PathVariable("id") Long id);

    @GetMapping("/api/mascotas/{id}")
    MascotaDto getMascota(@PathVariable("id") Long id);
}
