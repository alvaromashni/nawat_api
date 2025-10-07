package br.com.smartmesquitaapi.gateway;

import java.math.BigDecimal;

/**
 * DTO que representa um resultado padronizado
 * de uma operação de processamento de pagamento.
 *
 * @param status Status da transação (ex: "success", "failed").
 * @param transactionId O identificador único da transação gerado pelo gateway.
 * @param paidValue O montante exato que foi confirmado pelo gateway.
 * @param errorMessage Uma mensagem descritiva em caso de falha (pode ser nulo).
 */

public record ResultPaymentGateway(
        String status,
        String transactionId,
        BigDecimal paidValue,
        String errorMessage
) {

    public static ResultPaymentGateway success(String transactionId, BigDecimal paidValue){
        return new ResultPaymentGateway("success", transactionId, paidValue, null);
    }

    public static ResultPaymentGateway failed(String errorMessage){
        return new ResultPaymentGateway("failed", null, null, errorMessage);
    }

}
