package br.com.smartmesquitaapi.service;

import br.com.smartmesquitaapi.gateway.DealPaymentGateway;
import br.com.smartmesquitaapi.gateway.ResultPaymentGateway;
import br.com.smartmesquitaapi.model.Donation;
import br.com.smartmesquitaapi.model.StatusDonation;
import br.com.smartmesquitaapi.repository.DonationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DonationService {

    private final DealPaymentGateway paymentGateway;
    private final DonationRepository donationRepository;

    public Donation createDonation(BigDecimal value, String tokenCard, Long idInstitution){
        ResultPaymentGateway resultPaymentGateway = paymentGateway.processPayment(value, tokenCard);

        Donation newDonation = new Donation();
        newDonation.setDonationValue(resultPaymentGateway.paidValue());
        newDonation.setIdInstitution(idInstitution);
        newDonation.setIdTransactionGateway(resultPaymentGateway.transactionId());

        if("success".equals(resultPaymentGateway.status())){
            newDonation.setStatus(StatusDonation.APPROVED);
        }
        else if("refused".equals(resultPaymentGateway.status())){
            newDonation.setStatus(StatusDonation.REFUSED);
        }

        return donationRepository.save(newDonation);
    }

}
