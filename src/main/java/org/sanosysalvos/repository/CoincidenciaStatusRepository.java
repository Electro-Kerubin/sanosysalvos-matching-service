package org.sanosysalvos.repository;

import java.util.Optional;
import org.sanosysalvos.model.CoincidenciaStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoincidenciaStatusRepository extends JpaRepository<CoincidenciaStatus, Long> {

    Optional<CoincidenciaStatus> findByDescripcion(String descripcion);
}

