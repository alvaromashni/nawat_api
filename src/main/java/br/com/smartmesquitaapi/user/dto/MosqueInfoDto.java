package br.com.smartmesquitaapi.user.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MosqueInfoDto {

    private String mosqueName;
    private String imaName;
    private String phoneNumber;
    private LocalDate foundationDate;
    private String administratorName;
    private String cnpj;
    private String openingHours;

}
