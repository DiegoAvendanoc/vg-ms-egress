package pe.edu.vallegrande.vg_ms_egress.domain.dto;
import lombok.Data;

@Data
public class NotificationResponse {
    private boolean success;
    private String message;
}
