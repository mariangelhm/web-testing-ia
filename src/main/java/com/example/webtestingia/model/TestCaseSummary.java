package com.example.webtestingia.model;

import java.util.List;

/**
 * Resumen de un caso de prueba para listados generales.
 */
public class TestCaseSummary {

    private String ruta;
    private String escenario;
    private List<String> tags;
    private QualityResult calidad;

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    public String getEscenario() {
        return escenario;
    }

    public void setEscenario(String escenario) {
        this.escenario = escenario;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public QualityResult getCalidad() {
        return calidad;
    }

    public void setCalidad(QualityResult calidad) {
        this.calidad = calidad;
    }
}
