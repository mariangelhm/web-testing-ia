package com.example.webtestingia.service;

import com.example.webtestingia.model.exception.FileAccessException;
import com.example.webtestingia.model.exception.InvalidConfigurationException;
import com.example.webtestingia.model.exception.ParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio que carga y resuelve locators definidos en archivos YAML por proyecto.
 */
@Service
public class LocatorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocatorService.class);
    private final Path locatorsRoot = Paths.get("src/main/resources/locators");
    private final Map<String, Map<String, Map<String, String>>> cache = new ConcurrentHashMap<>();
    private final Yaml yaml = new Yaml();

    /**
     * Obtiene todos los grupos y locators de un proyecto.
     *
     * @param proyecto nombre del proyecto.
     * @return mapa de grupos a sus locators.
     */
    public Map<String, Map<String, String>> obtenerLocators(String proyecto) {
        cargarProyectoSiNoExiste(proyecto);
        return cache.getOrDefault(proyecto, Collections.emptyMap());
    }

    /**
     * Obtiene los locators de un grupo específico dentro de un proyecto.
     *
     * @param proyecto nombre del proyecto.
     * @param grupo    nombre del grupo.
     * @return mapa de locators.
     */
    public Map<String, String> obtenerGrupo(String proyecto, String grupo) {
        cargarProyectoSiNoExiste(proyecto);
        Map<String, Map<String, String>> grupos = cache.getOrDefault(proyecto, Collections.emptyMap());
        if (!grupos.containsKey(grupo)) {
            throw new InvalidConfigurationException("Grupo de locators no encontrado: " + grupo);
        }
        return grupos.get(grupo);
    }

    /**
     * Resuelve un locator por nombre usando el grupo actual.
     *
     * @param proyecto nombre del proyecto.
     * @param grupo    grupo de locators.
     * @param nombre   clave del locator.
     * @return selector resuelto.
     */
    public String resolveLocator(String proyecto, String grupo, String nombre) {
        Map<String, String> locators = obtenerGrupo(proyecto, grupo);
        if (!locators.containsKey(nombre)) {
            throw new InvalidConfigurationException("Locator no encontrado: " + nombre);
        }
        String selector = locators.get(nombre);
        if (selector == null || selector.isBlank()) {
            throw new InvalidConfigurationException("Selector vacío para locator: " + nombre);
        }
        return selector;
    }

    private synchronized void cargarProyectoSiNoExiste(String proyecto) {
        if (cache.containsKey(proyecto)) {
            return;
        }
        Path projectFolder = locatorsRoot.resolve(proyecto);
        if (!Files.exists(projectFolder)) {
            LOGGER.warn("No existe carpeta de locators para {}", proyecto);
            cache.put(proyecto, new HashMap<>());
            return;
        }
        Map<String, Map<String, String>> grupos = new HashMap<>();
        try {
            Files.list(projectFolder)
                    .filter(path -> path.toString().endsWith(".yml"))
                    .forEach(path -> cargarArchivo(grupos, path));
            cache.put(proyecto, grupos);
        } catch (IOException e) {
            throw new FileAccessException("Error al leer locators de " + proyecto, e);
        }
    }

    private void cargarArchivo(Map<String, Map<String, String>> grupos, Path path) {
        try (InputStream inputStream = Files.newInputStream(path)) {
            Object data = yaml.load(inputStream);
            if (data instanceof Map<?, ?> mapa) {
                mapa.forEach((grupo, contenido) -> {
                    Map<String, String> valores = new HashMap<>();
                    if (contenido instanceof Map<?, ?> submapa) {
                        submapa.forEach((k, v) -> valores.put(String.valueOf(k), String.valueOf(v)));
                    }
                    grupos.put(String.valueOf(grupo), valores);
                });
                LOGGER.info("Locators cargados desde {}", path.getFileName());
            } else {
                throw new ParsingException("Estructura YAML inválida en " + path.getFileName(), null);
            }
        } catch (IOException e) {
            throw new FileAccessException("No se pudo leer el archivo de locators " + path, e);
        }
    }
}
