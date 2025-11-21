package com.example.webtestingia.model.exception;

/**
 * Excepción lanzada ante problemas al leer o escribir en el sistema de archivos.
 */
public class FileAccessException extends RuntimeException {

    /**
     * Constructor con causa original.
     *
     * @param message mensaje de error.
     * @param cause   causa raíz.
     */
    public FileAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
