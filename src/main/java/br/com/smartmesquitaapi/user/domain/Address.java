package br.com.smartmesquitaapi.user.domain;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class Address {

    private String street;
    private String number;
    private String neighborhood;
    private String zipcode;
    private String complement;
    private String city;
    private String state;

}
