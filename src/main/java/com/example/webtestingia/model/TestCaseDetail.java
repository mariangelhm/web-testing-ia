package com.example.webtestingia.model;

import java.util.List;

/**
 * Representa el detalle completo de un caso de prueba y su análisis.
 */
public class TestCaseDetail {

    private String ruta;
    private String contenido;
    private List<String> escenarios;
    private QualityResult calidad;

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public List<String> getEscenarios() {
        return escenarios;
    }

    public void setEscenarios(List<String> escenarios) {
        this.escenarios = escenarios;
    }

    public QualityResult getCalidad() {
        return calidad;
    }

    public void setCalidad(QualityResult calidad) {
        this.calidad = calidad;
    }
}
