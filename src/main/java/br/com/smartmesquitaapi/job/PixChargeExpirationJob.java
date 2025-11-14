package br.com.smartmesquitaapi.job;

import br.com.smartmesquitaapi.service.pix.PixChargeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Job para expirar cobranças PIX antigas
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PixChargeExpirationJob {

    private final PixChargeService pixChargeService;

    @Scheduled(fixedDelay = 300000)
    public void expireOldChanges(){
        try{
            log.debug("Iniciando job de expiração de cobranças PIX");
            int expiredCount = pixChargeService.expireOldCharges();

            if (expiredCount > 0){
                log.info("Job de expiração concluído: {} cobranças expiradas.", expiredCount);
            }
        } catch(Exception e){
            log.error("Erro ao executar job de expiração.", e);
        }
    }
}

