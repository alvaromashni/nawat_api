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

    /**
     * Busca cobrança por user_id e idempotency_key (para garantir idempotência)
     */
    @Query("SELECT pc FROM PixCharge pc WHERE pc.user.userId = :userId AND pc.idempotencyKey = :idempotencyKey")
    Optional<PixCharge> findByUserIdAndIdempotencyKey(
            @Param("userId") UUID userId,
            @Param("idempotencyKey") String idempotencyKey
    );

    /**
     * Busca cobrança por txid (transaction ID do PIX)
     */
    Optional<PixCharge> findByTxid(String txid);

    /**
     * Busca cobrança por local_donation_id (ID gerado pelo totem)
     */
    Optional<PixCharge> findByLocalDonationId(String localDonationId);

    /**
     * Lista todas as cobranças de um usuário específico
     */
    @Query("SELECT pc FROM PixCharge pc WHERE pc.user.userId = :userId ORDER BY pc.createdAt DESC")
    List<PixCharge> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Lista cobranças por status
     */
    List<PixCharge> findByStatusOrderByCreatedAtDesc(PixChargeStatus status);

    /**
     * Lista cobranças pendentes de um usuário específico
     */
    @Query("SELECT pc FROM PixCharge pc WHERE pc.user.userId = :userId AND pc.status = :status ORDER BY pc.createdAt DESC")
    List<PixCharge> findByUserIdAndStatusOrderByCreatedAtDesc(
            @Param("userId") UUID userId,
            @Param("status") PixChargeStatus status,
            Pageable pageable
    );

    /**
     * Busca cobranças pendentes expiradas (para job de expiração)
     */
    @Query("SELECT pc FROM PixCharge pc WHERE pc.status = 'PENDING' AND pc.expiresAt < :now")
    List<PixCharge> findExpiredPendingCharges(@Param("now") LocalDateTime now);

    /**
     * Busca cobranças pendentes antigas (para reconciliação)
     * @param status Status da cobrança
     * @param thresholdTime Data/hora limite (cobranças antes disso)
     */
    @Query("SELECT pc FROM PixCharge pc WHERE pc.status = :status " +
            "AND pc.createdAt < :thresholdTime " +
            "ORDER BY pc.createdAt ASC")
    List<PixCharge> findOldChargesByStatus(
            @Param("status") PixChargeStatus status,
            @Param("thresholdTime") LocalDateTime thresholdTime,
            Pageable pageable
    );

    /**
     * Busca cobranças por valor e intervalo de tempo (para reconciliação por heurística)
     * @param amountCents Valor em centavos
     * @param startTime Início do intervalo
     * @param endTime Fim do intervalo
     */
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

    /**
     * Conta cobranças por status e "user"
     */
    @Query("SELECT COUNT(pc) FROM PixCharge pc WHERE pc.user.userId = :userId AND pc.status = :status")
    Long countByUserAndStatus(@Param("userId") UUID userId, @Param("status") PixChargeStatus status);

    /**
     * Lista cobranças criadas num período específico
     */
    @Query("SELECT pc FROM PixCharge pc WHERE pc.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY pc.createdAt DESC")
    List<PixCharge> findChargesInPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * Lista cobranças confirmadas manualmente pendentes de revisão
     */
    @Query("SELECT pc FROM PixCharge pc WHERE pc.status = 'CONFIRMED_MANUAL' " +
            "AND pc.confirmedAt >= :since " +
            "ORDER BY pc.confirmedAt DESC")
    List<PixCharge> findRecentManuallyConfirmedCharges(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * Verifica se existe cobrança com mesmo txid
     */
    boolean existsByTxid(String txid);

    /**
     * Busca última cobrança de um "user"
     */
    @Query("SELECT pc FROM PixCharge pc WHERE pc.user.userId = :userId ORDER BY pc.createdAt DESC LIMIT 1")
    Optional<PixCharge> findFirstByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Lista cobranças com QR expirado, mas ainda PENDING (inconsistência)
     */
    @Query("SELECT pc FROM PixCharge pc WHERE pc.status = 'PENDING' " +
            "AND pc.expiresAt < :now " +
            "AND pc.updatedAt < :gracePeriod")
    List<PixCharge> findStuckPendingCharges(
            @Param("now") LocalDateTime now,
            @Param("gracePeriod") LocalDateTime gracePeriod,
            Pageable pageable
    );

    /**
     * Soma de valores por status e período
     */
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