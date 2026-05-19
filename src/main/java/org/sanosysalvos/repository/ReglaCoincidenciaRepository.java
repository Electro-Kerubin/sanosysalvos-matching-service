package org.sanosysalvos.repository;

import java.util.List;
import org.sanosysalvos.model.ReglaCoincidencia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReglaCoincidenciaRepository extends JpaRepository<ReglaCoincidencia, Long> {

    List<ReglaCoincidencia> findByIsActiveTrue();
}

