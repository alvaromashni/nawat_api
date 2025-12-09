package br.com.smartmesquitaapi.organization.repository;

import br.com.smartmesquitaapi.organization.domain.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {


    Optional<Organization> findByCnpj(String cnpj);

    boolean existsByCnpj(String cnpj);

    List<Organization> findByIsEnabledTrue();

    Page<Organization> findByIsEnabledTrue(Pageable pageable);

    Page<Organization> findAll(Pageable pageable);

    @Query("SELECT o FROM Organization o WHERE LOWER(o.address.city) = LOWER(:city)")
    List<Organization> findByCity(@Param("city") String city);

    @Query("SELECT o FROM Organization o WHERE LOWER(o.address.state) = LOWER(:state)")
    List<Organization> findByState(@Param("state") String state);

    @Query("SELECT o FROM Organization o WHERE LOWER(o.orgName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Organization> findByOrgNameContainingIgnoreCase(@Param("name") String name);

    @Query("SELECT o FROM Organization o WHERE o.isEnabled = true AND o.bankDetails.isVerified = true")
    List<Organization> findOrganizationsAbleToReceivePayments();

    long countByIsEnabledTrue();
}
