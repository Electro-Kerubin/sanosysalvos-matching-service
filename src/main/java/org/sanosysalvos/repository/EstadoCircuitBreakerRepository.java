package org.sanosysalvos.repository;

import java.util.Optional;
import org.sanosysalvos.model.EstadoCircuitBreaker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstadoCircuitBreakerRepository extends JpaRepository<EstadoCircuitBreaker, Long> {

    Optional<EstadoCircuitBreaker> findByDescripcion(String descripcion);
}

