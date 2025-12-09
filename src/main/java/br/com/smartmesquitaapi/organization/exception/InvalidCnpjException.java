package br.com.smartmesquitaapi.organization.exception;

import br.com.smartmesquitaapi.api.exception.ApplicationException;
import org.springframework.http.HttpStatus;

public class InvalidCnpjException extends ApplicationException {

    public InvalidCnpjException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public InvalidCnpjException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST);
    }
}