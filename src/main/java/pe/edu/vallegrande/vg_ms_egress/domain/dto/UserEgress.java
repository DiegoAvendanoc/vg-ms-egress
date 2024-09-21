package pe.edu.vallegrande.vg_ms_egress.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserEgress {
    private String personId;
    private String categoryId;  
    private char type;
    private List<String> fileUrls;
    private char statusPayment;
    private boolean statusNotification;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}