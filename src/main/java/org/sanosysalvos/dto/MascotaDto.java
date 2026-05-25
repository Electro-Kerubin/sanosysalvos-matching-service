package org.sanosysalvos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MascotaDto {

    private String descripcionRaza;
    private String colorPrimario;
    private String colorSecundario;
    private String tamano;

    public String getDescripcionRaza() { return descripcionRaza; }
    public void setDescripcionRaza(String descripcionRaza) { this.descripcionRaza = descripcionRaza; }

    public String getColorPrimario() { return colorPrimario; }
    public void setColorPrimario(String colorPrimario) { this.colorPrimario = colorPrimario; }

    public String getColorSecundario() { return colorSecundario; }
    public void setColorSecundario(String colorSecundario) { this.colorSecundario = colorSecundario; }

    public String getTamano() { return tamano; }
    public void setTamano(String tamano) { this.tamano = tamano; }
}
