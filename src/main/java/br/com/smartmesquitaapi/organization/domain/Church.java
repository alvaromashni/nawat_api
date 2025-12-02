package br.com.smartmesquitaapi.organization.domain;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class Church extends Organization {
    private String priestName;
}
