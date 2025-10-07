package br.com.smartmesquitaapi.controller;


import br.com.smartmesquitaapi.dto.CreateDonationRequest;
import br.com.smartmesquitaapi.model.Donation;
import br.com.smartmesquitaapi.service.DonationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/donations")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;

    @PostMapping("post/donation")
    @ResponseStatus(HttpStatus.CREATED)
    public Donation createNewDonation(@Valid @RequestBody CreateDonationRequest request){
        return donationService.createDonation(
                request.value(),
                request.tokenCard(),
                request.idInstitution()
        );
    }
}
