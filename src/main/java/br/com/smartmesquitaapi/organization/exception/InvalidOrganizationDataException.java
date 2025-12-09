package br.com.smartmesquitaapi.organization.exception;

import br.com.smartmesquitaapi.api.exception.ApplicationException;
import org.springframework.http.HttpStatus;

public class InvalidOrganizationDataException extends ApplicationException {

    public InvalidOrganizationDataException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public InvalidOrganizationDataException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST);
    }
}