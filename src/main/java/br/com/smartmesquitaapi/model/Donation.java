package br.com.smartmesquitaapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "donations")
@Getter
@Setter
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal donationValue; // valor da doação

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime donatedAt; // data de criação da doação

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusDonation status;

    @Column(nullable = false)
    private Long idInstitution;

    @Column(unique = true)
    private String idTransactionGateway; // id da transação do gateway
}
