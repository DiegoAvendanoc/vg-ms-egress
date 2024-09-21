package pe.edu.vallegrande.vg_ms_egress.presentation.controller.Category;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vg_ms_egress.application.service.CategoryService;
import pe.edu.vallegrande.vg_ms_egress.domain.model.Category;
import reactor.core.publisher.Flux;


@RestController
@RequiredArgsConstructor
@RequestMapping("/community/egress_category/v1")
public class CategoryCommunityController {
    private final CategoryService categoryService;


    @GetMapping("/list")
    public Flux<Category> getAllAccountings() {
        return categoryService.getAll();
    }


}
