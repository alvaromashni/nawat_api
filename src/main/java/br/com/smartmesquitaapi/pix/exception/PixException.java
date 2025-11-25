package br.com.smartmesquitaapi.pix.exception;

import br.com.smartmesquitaapi.api.exception.ApplicationException;
import org.springframework.http.HttpStatus;

/**
 * Exceção base para erros relacionados a PIX
 */
public class PixException extends ApplicationException {

    public PixException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public PixException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }

    public PixException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST);
    }

    public PixException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause, httpStatus);
    }
}
