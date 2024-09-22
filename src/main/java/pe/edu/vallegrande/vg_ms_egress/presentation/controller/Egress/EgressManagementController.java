package pe.edu.vallegrande.vg_ms_egress.presentation.controller.Egress;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.vallegrande.vg_ms_egress.application.service.EgressService;
import pe.edu.vallegrande.vg_ms_egress.domain.dto.AdminEgressDto;
import pe.edu.vallegrande.vg_ms_egress.domain.dto.UserEgress;
import pe.edu.vallegrande.vg_ms_egress.domain.model.Egress;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/management/egress/v1")
public class EgressManagementController {
    private final EgressService egressService;

    public EgressManagementController(EgressService egressService) {
        this.egressService = egressService;
    }
    
    @GetMapping("/list")
    public Flux<Egress> getAllEgress() {
        return egressService.listAllEgress();
    }

    @PostMapping("/create")
    public Mono<ResponseEntity<Egress>> createEgress(
            @ModelAttribute UserEgress userDto,
            @RequestParam("files") MultipartFile[] files) {
        return egressService.createEgress(userDto, files)
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }


    @PatchMapping("/update/{egressId}")
    public Mono<ResponseEntity<Egress>> updateEgress(@PathVariable String egressId,
                                                     @RequestBody AdminEgressDto adminDto) {
        return egressService.updateEgress(egressId, adminDto)
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }


}
