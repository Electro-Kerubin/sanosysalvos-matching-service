package org.sanosysalvos.config;

import java.math.BigDecimal;
import java.util.List;
import org.sanosysalvos.model.CoincidenciaStatus;
import org.sanosysalvos.model.EstadoCircuitBreaker;
import org.sanosysalvos.model.ReglaCoincidencia;
import org.sanosysalvos.repository.CoincidenciaStatusRepository;
import org.sanosysalvos.repository.EstadoCircuitBreakerRepository;
import org.sanosysalvos.repository.ReglaCoincidenciaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializerConfig {

    @Bean
    CommandLineRunner seedCatalogs(
            CoincidenciaStatusRepository statusRepository,
            EstadoCircuitBreakerRepository estadoCircuitBreakerRepository,
            ReglaCoincidenciaRepository reglaCoincidenciaRepository
    ) {
        return args -> {
            seedStatuses(statusRepository);
            seedCircuitBreakerStates(estadoCircuitBreakerRepository);
            seedDefaultRules(reglaCoincidenciaRepository);
        };
    }

    private void seedStatuses(CoincidenciaStatusRepository statusRepository) {
        List<String> statuses = List.of("PENDIENTE", "PROCESADO", "FALLIDO");
        for (String status : statuses) {
            statusRepository.findByDescripcion(status).orElseGet(() -> {
                CoincidenciaStatus coincidenciaStatus = new CoincidenciaStatus();
                coincidenciaStatus.setDescripcion(status);
                return statusRepository.save(coincidenciaStatus);
            });
        }
    }

    private void seedCircuitBreakerStates(EstadoCircuitBreakerRepository estadoCircuitBreakerRepository) {
        List<String> estados = List.of("CLOSED", "OPEN", "HALF_OPEN");
        for (String estado : estados) {
            estadoCircuitBreakerRepository.findByDescripcion(estado).orElseGet(() -> {
                EstadoCircuitBreaker entity = new EstadoCircuitBreaker();
                entity.setDescripcion(estado);
                return estadoCircuitBreakerRepository.save(entity);
            });
        }
    }

    private void seedDefaultRules(ReglaCoincidenciaRepository reglaCoincidenciaRepository) {
        if (!reglaCoincidenciaRepository.findByIsActiveTrue().isEmpty()) {
            return;
        }

        createRule(reglaCoincidenciaRepository, "raza", BigDecimal.valueOf(0.25));
        createRule(reglaCoincidenciaRepository, "color", BigDecimal.valueOf(0.20));
        createRule(reglaCoincidenciaRepository, "tamano", BigDecimal.valueOf(0.20));
        createRule(reglaCoincidenciaRepository, "distancia", BigDecimal.valueOf(0.20));
        createRule(reglaCoincidenciaRepository, "fecha", BigDecimal.valueOf(0.15));
    }

    private void createRule(ReglaCoincidenciaRepository repository, String descripcion, BigDecimal importancia) {
        ReglaCoincidencia rule = new ReglaCoincidencia();
        rule.setDescripcion(descripcion);
        rule.setImportancia(importancia);
        rule.setIsActive(true);
        repository.save(rule);
    }
}

