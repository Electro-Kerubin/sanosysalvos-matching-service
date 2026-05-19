package org.sanosysalvos.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import org.sanosysalvos.model.ReporteMascota;
import org.springframework.stereotype.Service;

@Service
public class ScoringService {

    public BigDecimal scoreRaza(ReporteMascota perdido, ReporteMascota encontrado) {
        return exactMatch(perdido.getRaza(), encontrado.getRaza()) ? hundred() : BigDecimal.ZERO;
    }

    public BigDecimal scoreColor(ReporteMascota perdido, ReporteMascota encontrado) {
        if (perdido.getColor() == null || encontrado.getColor() == null) {
            return BigDecimal.ZERO;
        }

        String colorPerdido = perdido.getColor().trim().toLowerCase();
        String colorEncontrado = encontrado.getColor().trim().toLowerCase();

        if (colorPerdido.equals(colorEncontrado)) {
            return hundred();
        }

        if (colorPerdido.contains(colorEncontrado) || colorEncontrado.contains(colorPerdido)) {
            return BigDecimal.valueOf(60);
        }

        return BigDecimal.ZERO;
    }

    public BigDecimal scoreTamano(ReporteMascota perdido, ReporteMascota encontrado) {
        return exactMatch(perdido.getTamano(), encontrado.getTamano()) ? hundred() : BigDecimal.ZERO;
    }

    public BigDecimal scoreDistancia(ReporteMascota perdido, ReporteMascota encontrado) {
        if (perdido.getLatitud() == null
                || perdido.getLongitud() == null
                || encontrado.getLatitud() == null
                || encontrado.getLongitud() == null) {
            return BigDecimal.ZERO;
        }

        double distanceKm = haversine(
                perdido.getLatitud(),
                perdido.getLongitud(),
                encontrado.getLatitud(),
                encontrado.getLongitud()
        );

        if (distanceKm <= 2.0) {
            return hundred();
        }
        if (distanceKm <= 10.0) {
            return BigDecimal.valueOf(70);
        }
        if (distanceKm <= 30.0) {
            return BigDecimal.valueOf(40);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal scoreFecha(ReporteMascota perdido, ReporteMascota encontrado) {
        if (perdido.getFechaReporte() == null || encontrado.getFechaReporte() == null) {
            return BigDecimal.ZERO;
        }

        long diffDays = Math.abs(Duration.between(perdido.getFechaReporte(), encontrado.getFechaReporte()).toDays());

        if (diffDays <= 1) {
            return hundred();
        }
        if (diffDays <= 3) {
            return BigDecimal.valueOf(70);
        }
        if (diffDays <= 7) {
            return BigDecimal.valueOf(40);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal calcularPuntajeTotal(
            BigDecimal puntajeRaza,
            BigDecimal puntajeColor,
            BigDecimal puntajeTamano,
            BigDecimal puntajeDistancia,
            BigDecimal puntajeFecha,
            BigDecimal pesoRaza,
            BigDecimal pesoColor,
            BigDecimal pesoTamano,
            BigDecimal pesoDistancia,
            BigDecimal pesoFecha
    ) {
        BigDecimal sumaPesos = pesoRaza.add(pesoColor).add(pesoTamano).add(pesoDistancia).add(pesoFecha);
        if (sumaPesos.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal ponderado = puntajeRaza.multiply(pesoRaza)
                .add(puntajeColor.multiply(pesoColor))
                .add(puntajeTamano.multiply(pesoTamano))
                .add(puntajeDistancia.multiply(pesoDistancia))
                .add(puntajeFecha.multiply(pesoFecha));

        return ponderado.divide(sumaPesos, 2, RoundingMode.HALF_UP);
    }

    public String veredicto(BigDecimal puntajeTotal) {
        if (puntajeTotal.compareTo(BigDecimal.valueOf(75)) >= 0) {
            return "COINCIDENCIA_ALTA";
        }
        if (puntajeTotal.compareTo(BigDecimal.valueOf(50)) >= 0) {
            return "COINCIDENCIA_MEDIA";
        }
        return "COINCIDENCIA_BAJA";
    }

    private BigDecimal hundred() {
        return BigDecimal.valueOf(100);
    }

    private boolean exactMatch(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return left.trim().equalsIgnoreCase(right.trim());
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }
}

