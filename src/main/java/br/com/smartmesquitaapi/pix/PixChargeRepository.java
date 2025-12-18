package br.com.smartmesquitaapi.pix;

import br.com.smartmesquitaapi.pix.domain.PixCharge;
import br.com.smartmesquitaapi.pix.domain.PixChargeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface PixChargeRepository extends JpaRepository<PixCharge, UUID> {


    @Query("SELECT pc FROM PixCharge pc WHERE pc.organization.id = :organizationId AND pc.idempotencyKey = :idempotencyKey")
    Optional<PixCharge> findByOrganizationIdAndIdempotencyKey(
            @Param("organizationId") UUID organizationId,
            @Param("idempotencyKey") String idempotencyKey
    );


    Optional<PixCharge> findByTxid(String txid);


    Optional<PixCharge> findByLocalDonationId(String localDonationId);


    @Query("SELECT pc FROM PixCharge pc WHERE pc.user.userId = :userId ORDER BY pc.createdAt DESC")
    List<PixCharge> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId, Pageable pageable);

    List<PixCharge> findByStatusOrderByCreatedAtDesc(PixChargeStatus status);

    @Query("SELECT pc FROM PixCharge pc WHERE pc.user.userId = :userId AND pc.status = :status ORDER BY pc.createdAt DESC")
    List<PixCharge> findByUserIdAndStatusOrderByCreatedAtDesc(
            @Param("userId") UUID userId,
            @Param("status") PixChargeStatus status,
            Pageable pageable
    );


    @Query("SELECT pc FROM PixCharge pc WHERE pc.status = 'PENDING' AND pc.expiresAt < :now")
    List<PixCharge> findExpiredPendingCharges(@Param("now") LocalDateTime now);


    @Query("SELECT pc FROM PixCharge pc WHERE pc.status = :status " +
            "AND pc.createdAt < :thresholdTime " +
            "ORDER BY pc.createdAt ASC")
    List<PixCharge> findOldChargesByStatus(
            @Param("status") PixChargeStatus status,
            @Param("thresholdTime") LocalDateTime thresholdTime,
            Pageable pageable
    );

    @Query("SELECT pc FROM PixCharge pc WHERE pc.amountCents = :amountCents " +
            "AND pc.status = 'PENDING' " +
            "AND pc.createdAt BETWEEN :startTime AND :endTime " +
            "ORDER BY pc.createdAt ASC")
    List<PixCharge> findPendingChargesByAmountAndTimeRange(
            @Param("amountCents") Integer amountCents,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable
    );


    @Query("SELECT COUNT(p) FROM PixCharge p WHERE p.organization.id = :orgId AND p.status = :status")
    Long countByOrganizationIdAndStatus(@Param("orgId") UUID orgId, @Param("status") PixChargeStatus status);


    @Query("SELECT pc FROM PixCharge pc WHERE pc.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY pc.createdAt DESC")
    List<PixCharge> findChargesInPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );


    @Query("SELECT pc FROM PixCharge pc WHERE pc.status = 'CONFIRMED_MANUAL' " +
            "AND pc.confirmedAt >= :since " +
            "ORDER BY pc.confirmedAt DESC")
    List<PixCharge> findRecentManuallyConfirmedCharges(@Param("since") LocalDateTime since, Pageable pageable);


    boolean existsByTxid(String txid);


    @Query("SELECT pc FROM PixCharge pc WHERE pc.user.userId = :userId ORDER BY pc.createdAt DESC LIMIT 1")
    Optional<PixCharge> findFirstByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId, Pageable pageable);


    @Query("SELECT pc FROM PixCharge pc WHERE pc.status = 'PENDING' " +
            "AND pc.expiresAt < :now " +
            "AND pc.updatedAt < :gracePeriod")
    List<PixCharge> findStuckPendingCharges(
            @Param("now") LocalDateTime now,
            @Param("gracePeriod") LocalDateTime gracePeriod,
            Pageable pageable
    );


    @Query("SELECT SUM(pc.amountCents) FROM PixCharge pc " +
            "WHERE pc.status IN :statuses " +
            "AND pc.createdAt BETWEEN :startDate AND :endDate")
    Long sumAmountByStatusesAndPeriod(
            @Param("statuses") List<PixChargeStatus> statuses,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}