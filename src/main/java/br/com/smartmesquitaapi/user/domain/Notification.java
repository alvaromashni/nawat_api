package br.com.smartmesquitaapi.user.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@Builder
public class Notification implements Serializable {

    private boolean donationDone;
    private boolean dailySummary;
    private boolean totemMaintenance;

}
