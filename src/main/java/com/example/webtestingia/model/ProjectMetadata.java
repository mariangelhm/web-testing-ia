package com.example.webtestingia.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Representa el contenido del archivo project.json de cada proyecto.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectMetadata {

    private String id;
    private String nombre;
    private String codigoJira;
    private String tipo;
    private String autor;
    private String editor;
    private List<String> casos = new ArrayList<>();

    /**
     * Genera un metadato por defecto con valores básicos y un identificador único.
     *
     * @param nombreProyecto nombre de la carpeta del proyecto.
     * @return instancia inicializada.
     */
    public static ProjectMetadata defaultFor(String nombreProyecto) {
        ProjectMetadata metadata = new ProjectMetadata();
        metadata.setId(UUID.randomUUID().toString());
        metadata.setNombre(nombreProyecto);
        metadata.setCodigoJira(nombreProyecto.toUpperCase());
        metadata.setTipo("WEB");
        metadata.setAutor("Desconocido");
        metadata.setEditor("Desconocido");
        return metadata;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigoJira() {
        return codigoJira;
    }

    public void setCodigoJira(String codigoJira) {
        this.codigoJira = codigoJira;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getEditor() {
        return editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public List<String> getCasos() {
        return casos;
    }

    public void setCasos(List<String> casos) {
        this.casos = casos;
    }
}
