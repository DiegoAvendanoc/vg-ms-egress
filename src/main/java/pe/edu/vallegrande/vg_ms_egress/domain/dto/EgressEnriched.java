package pe.edu.vallegrande.vg_ms_egress.domain.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import pe.edu.vallegrande.vg_ms_egress.domain.model.Category;

@Data
public class EgressEnriched {
    private String egressId;
    private String personId;
    private String userConfirmedId;
    private List<Category> categories;
    private char type;
    private List<String> fileUrls;
    private String comment;
    private char statusPayment;
    private boolean statusNotification;
    private LocalDateTime createdAt;

    // User object
    private User user;
    private User celebrant;
    private User personConfirmed;

}
