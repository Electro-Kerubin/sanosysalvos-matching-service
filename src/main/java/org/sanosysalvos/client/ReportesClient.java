package org.sanosysalvos.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ReportesClient {

    private final RestTemplate restTemplate;
    private final String reportesBaseUrl;

    public ReportesClient(
            RestTemplate restTemplate,
            @Value("${reportes.service.url:http://reportes:8083}") String reportesBaseUrl
    ) {
        this.restTemplate = restTemplate;
        this.reportesBaseUrl = reportesBaseUrl;
    }

    public ReporteContactoInfo obtenerInfoReporte(Long idReporteMascota) {
        String url = reportesBaseUrl + "/api/reportes/" + idReporteMascota;
        return restTemplate.getForObject(url, ReporteContactoInfo.class);
    }

    /** Subconjunto de ReporteMascotaDTO que necesitamos para notificar. */
    public static class ReporteContactoInfo {
        public Integer idReporteMascota;
        public Integer idContacto;
        public String nombresContacto;
        public String correoContacto;
        public String nombreMascota;
    }
}
