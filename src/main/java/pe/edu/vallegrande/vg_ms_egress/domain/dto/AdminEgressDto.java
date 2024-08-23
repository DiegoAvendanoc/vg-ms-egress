package pe.edu.vallegrande.vg_ms_egress.domain.dto;
import lombok.Data;

import java.util.List;

@Data
public class AdminEgressDto {
    private char statusPayment;
    private boolean statusNotification;
    private String comment;
    private String userConfirmedId;
}