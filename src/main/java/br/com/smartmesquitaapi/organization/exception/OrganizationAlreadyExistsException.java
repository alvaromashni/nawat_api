package br.com.smartmesquitaapi.organization.exception;

import br.com.smartmesquitaapi.api.exception.ApplicationException;
import org.springframework.http.HttpStatus;

public class OrganizationAlreadyExistsException extends ApplicationException {

    public OrganizationAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

    public OrganizationAlreadyExistsException(String message, Throwable cause) {
        super(message, cause, HttpStatus.CONFLICT);
    }
}