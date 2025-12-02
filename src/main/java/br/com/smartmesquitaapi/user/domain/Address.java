package br.com.smartmesquitaapi.user.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Embeddable
public class Address {

    private UUID addressId;
    private String street;
    private String number;
    private String neighborhood;
    private String zipcode;
    private String complement;
    private String city;
    private String state;

}
