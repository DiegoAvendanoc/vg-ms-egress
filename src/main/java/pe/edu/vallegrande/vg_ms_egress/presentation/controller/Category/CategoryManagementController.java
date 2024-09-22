package pe.edu.vallegrande.vg_ms_egress.presentation.controller.Category;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vg_ms_egress.application.service.CategoryService;
import pe.edu.vallegrande.vg_ms_egress.domain.model.Category;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/management/egress_category/v1")
public class CategoryManagementController {
    private final CategoryService categoryService;

    @GetMapping("/list")
    public Flux<Category> getAllAccountings() {
        return categoryService.getAll();
    }

    @PostMapping("/create")
    public Mono<Category> createCategory(@RequestBody Category category) {
        return categoryService.createCategory(category);
    }

}