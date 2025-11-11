package br.com.smartmesquitaapi.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "donations")
@Data
@Getter
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private BigDecimal donationValue; // valor da doação

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime donatedAt; // data de criação da doação

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusDonation status;

    @Column(nullable = false)
    private Long idInstitution;
}
