package org.sanosysalvos.repository;

import java.util.Optional;
import org.sanosysalvos.model.CoincidenciaResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoincidenciaResultRepository extends JpaRepository<CoincidenciaResult, Long> {

    Optional<CoincidenciaResult> findByCoincidenciaRequest_IdCoincidenciaRequest(Long idCoincidenciaRequest);
}

