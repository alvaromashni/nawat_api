package br.com.smartmesquitaapi.model.pix;

/**
 * Status possíveis de uma cobrança PIX
 */
public enum PixChargeStatus {

    /**
     * Cobrança criada, aguardando pagamento
     */
    PENDING,

    /**
     * Pagamento detectado automaticamente (via reconciliação/webhook)
     */
    PAID,

    /**
     * Pagamento confirmado manualmente por staff (via comprovante)
     */
    CONFIRMED_MANUAL,

    /**
     * QR Code expirado (passou do tempo de validade)
     */
    EXPIRED,

    /**
     * Cobrança cancelada
     */
    CANCELLED;

    /**
     * Verifica se o status indica que a cobrança foi finalizada
     */
    public boolean isFinal() {
        return this == PAID || this == CONFIRMED_MANUAL || this == EXPIRED || this == CANCELLED;
    }

    /**
     * Verifica se o status indica pagamento confirmado
     */
    public boolean isPaid() {
        return this == PAID || this == CONFIRMED_MANUAL;
    }
}
