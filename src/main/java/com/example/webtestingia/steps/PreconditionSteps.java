package com.example.webtestingia.steps;

import com.company.qa.core.db.DBHelper;
import com.company.qa.core.logging.LoggerUtil;
import com.company.qa.core.rest.RestServiceClient;
import io.cucumber.java.es.Dado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Steps que ejecutan precondiciones de datos o servicios usando la librería qa-core.
 */
@Component
public class PreconditionSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreconditionSteps.class);

    /**
     * Ejecuta una consulta previa identificada por nombre.
     *
     * @param nombre nombre lógico de la query.
     */
    @Dado("ejecuto la query previa \"{string}\"")
    public void ejecutoQuery(String nombre) {
        try {
            LOGGER.info("Ejecutando query previa {}", nombre);
            LoggerUtil.info("Query previa: " + nombre);
            DBHelper.executeNamedQuery(nombre);
        } catch (Exception e) {
            LOGGER.error("Error en query previa {}", nombre, e);
            throw new RuntimeException("Fallo en precondición de base de datos: " + e.getMessage(), e);
        }
    }

    /**
     * Ejecuta un servicio previo usando RestServiceClient.
     *
     * @param nombre identificador del servicio configurado.
     */
    @Dado("ejecuto el servicio previo \"{string}\"")
    public void ejecutoServicio(String nombre) {
        try {
            LOGGER.info("Ejecutando servicio previo {}", nombre);
            LoggerUtil.info("Servicio previo: " + nombre);
            RestServiceClient client = new RestServiceClient(nombre);
            client.execute();
        } catch (Exception e) {
            LOGGER.error("Error en servicio previo {}", nombre, e);
            throw new RuntimeException("Fallo en precondición de servicio: " + e.getMessage(), e);
        }
    }
}
