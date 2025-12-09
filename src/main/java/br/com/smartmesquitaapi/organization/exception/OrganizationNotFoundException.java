package br.com.smartmesquitaapi.organization.exception;

import br.com.smartmesquitaapi.api.exception.ApplicationException;
import org.springframework.http.HttpStatus;

public class OrganizationNotFoundException extends ApplicationException {

    public OrganizationNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    public OrganizationNotFoundException(String message, Throwable cause) {
        super(message, cause, HttpStatus.NOT_FOUND);
    }
}