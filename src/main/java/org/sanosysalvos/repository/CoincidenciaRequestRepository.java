package org.sanosysalvos.repository;

import java.util.List;
import org.sanosysalvos.model.CoincidenciaRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoincidenciaRequestRepository extends JpaRepository<CoincidenciaRequest, Long> {

    @EntityGraph(attributePaths = {"reportePerdido", "reporteEncontrado", "status"})
    List<CoincidenciaRequest> findByReportePerdido_IdReporteMascotaOrReporteEncontrado_IdReporteMascota(
            Long idPerdidoReporte,
            Long idEncontradoReporte
    );
}

