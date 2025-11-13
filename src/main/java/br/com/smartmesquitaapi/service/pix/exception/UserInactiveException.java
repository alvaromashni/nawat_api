package br.com.smartmesquitaapi.service.pix.exception;

/**
 * Usuário inativo
 */
public class UserInactiveException extends PixException {
    public UserInactiveException(String message) {
        super(message);
    }
}
