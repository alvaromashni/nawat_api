package br.com.smartmesquitaapi.service.pix.exception;


/**
 * Usuário não encontrado
 */
public class UserNotFoundException extends PixException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
