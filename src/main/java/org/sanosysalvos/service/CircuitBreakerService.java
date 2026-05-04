package org.sanosysalvos.service;

import java.time.LocalDateTime;
import org.sanosysalvos.exception.ExternalServiceException;
import org.sanosysalvos.exception.NotFoundException;
import org.sanosysalvos.model.CircuitBreakerEstado;
import org.sanosysalvos.model.EstadoCircuitBreaker;
import org.sanosysalvos.repository.CircuitBreakerEstadoRepository;
import org.sanosysalvos.repository.EstadoCircuitBreakerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CircuitBreakerService {

    private final CircuitBreakerEstadoRepository circuitBreakerEstadoRepository;
    private final EstadoCircuitBreakerRepository estadoCircuitBreakerRepository;
    private final int failureThreshold;
    private final int retrySeconds;

    public CircuitBreakerService(
            CircuitBreakerEstadoRepository circuitBreakerEstadoRepository,
            EstadoCircuitBreakerRepository estadoCircuitBreakerRepository,
            @Value("${matching.circuit-breaker.failure-threshold:3}") int failureThreshold,
            @Value("${matching.circuit-breaker.retry-seconds:60}") int retrySeconds
    ) {
        this.circuitBreakerEstadoRepository = circuitBreakerEstadoRepository;
        this.estadoCircuitBreakerRepository = estadoCircuitBreakerRepository;
        this.failureThreshold = failureThreshold;
        this.retrySeconds = retrySeconds;
    }

    @Transactional
    public String executeProtected(Runnable action) {
        CircuitBreakerEstado estado = getOrCreateState();
        LocalDateTime now = LocalDateTime.now();

        if (isOpenAndStillBlocked(estado, now)) {
            throw new ExternalServiceException("Circuit breaker OPEN. Reintentar despues de " + estado.getNextRetryAt());
        }

        if (isOpenAndReadyToRetry(estado, now)) {
            setState(estado, "HALF_OPEN");
        }

        try {
            action.run();
            recordSuccess(estado);
        } catch (RuntimeException ex) {
            recordFailure(estado, ex.getMessage());
            throw ex;
        }

        return estado.getEstadoCircuitbreaker().getDescripcion();
    }

    @Transactional(readOnly = true)
    public String getCurrentState() {
        return getOrCreateState().getEstadoCircuitbreaker().getDescripcion();
    }

    private boolean isOpenAndStillBlocked(CircuitBreakerEstado estado, LocalDateTime now) {
        return "OPEN".equals(estado.getEstadoCircuitbreaker().getDescripcion())
                && estado.getNextRetryAt() != null
                && now.isBefore(estado.getNextRetryAt());
    }

    private boolean isOpenAndReadyToRetry(CircuitBreakerEstado estado, LocalDateTime now) {
        return "OPEN".equals(estado.getEstadoCircuitbreaker().getDescripcion())
                && estado.getNextRetryAt() != null
                && !now.isBefore(estado.getNextRetryAt());
    }

    private void recordSuccess(CircuitBreakerEstado estado) {
        if ("HALF_OPEN".equals(estado.getEstadoCircuitbreaker().getDescripcion())
                || "OPEN".equals(estado.getEstadoCircuitbreaker().getDescripcion())) {
            setState(estado, "CLOSED");
            estado.setCantidadExitos(1);
            estado.setCantidadFallas(0);
            estado.setOpenedAt(null);
            estado.setNextRetryAt(null);
            estado.setLastError(null);
        } else {
            estado.setCantidadExitos(estado.getCantidadExitos() + 1);
            estado.setCantidadFallas(0);
        }

        estado.setUpdatedAt(LocalDateTime.now());
        circuitBreakerEstadoRepository.save(estado);
    }

    private void recordFailure(CircuitBreakerEstado estado, String error) {
        int totalFallas = estado.getCantidadFallas() + 1;
        estado.setCantidadFallas(totalFallas);
        estado.setCantidadExitos(0);
        estado.setLastError(error);

        if (totalFallas >= estado.getLimiteFallas()) {
            setState(estado, "OPEN");
            LocalDateTime now = LocalDateTime.now();
            estado.setOpenedAt(now);
            estado.setNextRetryAt(now.plusSeconds(retrySeconds));
        }

        estado.setUpdatedAt(LocalDateTime.now());
        circuitBreakerEstadoRepository.save(estado);
    }

    private void setState(CircuitBreakerEstado estado, String stateName) {
        estado.setEstadoCircuitbreaker(findBreakerState(stateName));
    }

    private CircuitBreakerEstado getOrCreateState() {
        return circuitBreakerEstadoRepository.findAll().stream().findFirst().orElseGet(() -> {
            CircuitBreakerEstado state = new CircuitBreakerEstado();
            state.setEstadoCircuitbreaker(findBreakerState("CLOSED"));
            state.setCantidadFallas(0);
            state.setCantidadExitos(0);
            state.setLimiteFallas(Math.max(1, failureThreshold));
            state.setUpdatedAt(LocalDateTime.now());
            return circuitBreakerEstadoRepository.save(state);
        });
    }

    private EstadoCircuitBreaker findBreakerState(String stateName) {
        return estadoCircuitBreakerRepository.findByDescripcion(stateName)
                .orElseThrow(() -> new NotFoundException("No existe estado_circuitbreaker: " + stateName));
    }
}

