package com.example.webtestingia.model.exception;

/**
 * Excepción lanzada cuando una configuración obligatoria no cumple con el formato esperado.
 */
public class InvalidConfigurationException extends RuntimeException {

    /**
     * Constructor con mensaje.
     *
     * @param message detalle de la configuración inválida.
     */
    public InvalidConfigurationException(String message) {
        super(message);
    }
}
