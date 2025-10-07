package br.com.smartmesquitaapi.gateway;

import java.math.BigDecimal;

public interface DealPaymentGateway {
    ResultPaymentGateway processPayment(BigDecimal valor, String tokenCard);
}

// Poderíamos ter um DTO para a resposta também
record ResultPayment(String status, String transactionId, BigDecimal paidValue) {}
