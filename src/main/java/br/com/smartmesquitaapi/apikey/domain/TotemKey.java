package br.com.smartmesquitaapi.apikey.domain;

import br.com.smartmesquitaapi.organization.domain.Organization;
import jakarta.persistence.*;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "totem_keys")
public class TotemKey {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;

    @Column(unique = true, nullable = false)
    private String keyValue;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Setter
    private boolean isActive = true;

    public TotemKey() {}

    public TotemKey(String name, String keyValue, Organization organization) {
        this.name = name;
        this.keyValue = keyValue;
        this.organization = organization;
    }

    // Getters e Setters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getKeyValue() { return keyValue; }
    public Organization getOrganization() { return organization; }
    public boolean isActive() { return isActive; }
}
