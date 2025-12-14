package br.com.smartmesquitaapi.apikey.repository;

import br.com.smartmesquitaapi.apikey.domain.TotemKey;
import br.com.smartmesquitaapi.organization.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TotemKeyRepository extends JpaRepository<TotemKey, UUID> {

    Optional<TotemKey> findByKeyValueAndIsActiveTrue(String keyValue);

    List<TotemKey> findByOrganizationOrderByCreatedAtDesc(Organization organization);

    List<TotemKey> findByOrganizationAndIsActiveTrueOrderByCreatedAtDesc(Organization organization);

}
