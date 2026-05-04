package org.sanosysalvos.service;

import java.util.Map;
import org.sanosysalvos.exception.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class HttpCoincidenciaNotifier implements CoincidenciaNotifier {

    private final RestTemplate restTemplate;
    private final String notificationUrl;

    public HttpCoincidenciaNotifier(
            RestTemplate restTemplate,
            @Value("${matching.notification.url:}") String notificationUrl
    ) {
        this.restTemplate = restTemplate;
        this.notificationUrl = notificationUrl;
    }

    @Override
    public void notificarCoincidenciaPotencial(Long idCoincidenciaRequest, String veredictoFinal) {
        if (notificationUrl == null || notificationUrl.isBlank()) {
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = Map.of(
                "idCoincidenciaRequest", idCoincidenciaRequest,
                "veredictoFinal", veredictoFinal
        );

        try {
            restTemplate.postForEntity(notificationUrl, new HttpEntity<>(payload, headers), Void.class);
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Error al notificar coincidencia potencial", ex);
        }
    }
}

