package br.com.smartmesquitaapi.user.dto;

import lombok.Data;

@Data
public class AddressDto {

    private String street;
    private String number;
    private String neighborhood;
    private String zipcode;
    private String complement;
    private String city;
    private String state;

}
