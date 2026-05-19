package org.sanosysalvos.service;

public interface CoincidenciaNotifier {

    void notificarCoincidenciaPotencial(Long idCoincidenciaRequest, String veredictoFinal);
}

