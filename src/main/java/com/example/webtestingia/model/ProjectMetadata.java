package com.example.webtestingia.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Representa el contenido del archivo project.json de cada proyecto.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectMetadata {

    private String id;

    @JsonProperty("name")
    @JsonAlias("nombre")
    private String name;

    @JsonProperty("jiraCode")
    @JsonAlias("codigoJira")
    private String jiraCode;

    @JsonProperty("type")
    @JsonAlias("tipo")
    private String type;

    @JsonProperty("author")
    @JsonAlias("autor")
    private String author;

    @JsonProperty("editor")
    @JsonAlias("editor")
    private String editor;

    @JsonProperty("cases")
    @JsonAlias("casos")
    private List<String> cases = new ArrayList<>();

    /**
     * Genera un metadato por defecto con valores básicos y un identificador único.
     *
     * @param nombreProyecto nombre de la carpeta del proyecto.
     * @return instancia inicializada.
     */
    public static ProjectMetadata defaultFor(String nombreProyecto) {
        ProjectMetadata metadata = new ProjectMetadata();
        metadata.setId(UUID.randomUUID().toString());
        metadata.setName(nombreProyecto);
        metadata.setJiraCode(nombreProyecto.toUpperCase());
        metadata.setType("WEB");
        metadata.setAuthor("Desconocido");
        metadata.setEditor("Desconocido");
        return metadata;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJiraCode() {
        return jiraCode;
    }

    public void setJiraCode(String jiraCode) {
        this.jiraCode = jiraCode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getEditor() {
        return editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public List<String> getCases() {
        return cases;
    }

    public void setCases(List<String> cases) {
        this.cases = cases;
    }
}
