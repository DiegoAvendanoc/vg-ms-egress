package pe.edu.vallegrande.vg_ms_egress.presentation.controller.Egress;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.vallegrande.vg_ms_egress.application.service.EgressService;
import pe.edu.vallegrande.vg_ms_egress.domain.dto.UserEgress;
import pe.edu.vallegrande.vg_ms_egress.domain.model.Egress;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("community/egress/v1")
public class EgressCommunityController {

    private final EgressService egressService;

    public EgressCommunityController(EgressService egressService) {
        this.egressService = egressService;
    }

    @PostMapping("/create")
    public Mono<ResponseEntity<Egress>> createEgress(
        @ModelAttribute UserEgress userDto,
        @RequestParam("files") MultipartFile[] files) {
        return egressService.createEgress(userDto, files)
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

}
