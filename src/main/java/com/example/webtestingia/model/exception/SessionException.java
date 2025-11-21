package com.example.webtestingia.model.exception;

/**
 * Excepción para representar problemas en el manejo de sesiones del grabador.
 */
public class SessionException extends RuntimeException {

    /**
     * Constructor con detalle.
     *
     * @param message descripción del problema.
     */
    public SessionException(String message) {
        super(message);
    }
}
