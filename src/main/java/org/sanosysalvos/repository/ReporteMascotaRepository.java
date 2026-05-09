package org.sanosysalvos.repository;

import java.util.List;
import org.sanosysalvos.model.ReporteMascota;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReporteMascotaRepository extends JpaRepository<ReporteMascota, Long> {

    /**
     * Busca todos los reportes que NO sean del tipo indicado.
     * Permite encontrar reportes del tipo opuesto (perdido vs encontrado).
     */
    List<ReporteMascota> findByIdTipoReporteNot(Integer idTipoReporte);
}

