package br.com.smartmesquitaapi.gatewayDeprecated;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@Profile("dev")
public class MockPaymentGatewayImpl implements DealPaymentGateway {

    @Override
    public ResultPaymentGateway processPayment(BigDecimal value, String tokenCard) {
        System.out.println("--- USANDO O GATEWAY DE PAGAMENTO MOCK (FAKE) ---");
        if (value.compareTo(BigDecimal.ONE) < 0) {

            // Simula uma falha para valores menores que R$1,00
            return ResultPaymentGateway.failed("Valor muito baixo para teste.");
        }
        // 3. Simula uma resposta de sucesso, retornando um ID de transação aleatório
        String transactionId = "mock_" + UUID.randomUUID().toString();
        System.out.println("--- Pagamento de R$" + value + " APROVADO (MOCK) com ID: "+transactionId+" ---");
        return ResultPaymentGateway.success(transactionId, value);
    }
}
