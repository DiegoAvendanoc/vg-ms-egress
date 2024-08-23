package pe.edu.vallegrande.vg_ms_egress.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;
import pe.edu.vallegrande.vg_ms_egress.domain.dto.User;

import java.time.LocalDateTime;
import java.util.List;
@Data
@Document(collection = "egress")
public class Egress {

    private String egressId;
    private String personId;
    private String categoryId;
    private char type; // por defecto (E) Egress
    private List<String> fileUrls;
    private String userConfirmedId;
    private String comment;
    private char statusPayment; // por defecto (A) Aceptado
    private boolean statusNotification;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User user;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User personConfirmed;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Category category;
}
