package br.com.smartmesquitaapi.service.pix.exception;

import org.springframework.http.HttpStatus;

/**
 * Exceção lançada quando ocorre erro na geração do QR Code
 */
public class QrCodeGenerationException extends PixException {

    public QrCodeGenerationException(String message, Throwable cause) {
        super(message, cause, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
