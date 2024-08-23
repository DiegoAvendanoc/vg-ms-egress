package pe.edu.vallegrande.vg_ms_egress.domain.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vg_ms_egress.domain.model.Category;
import reactor.core.publisher.Mono;

@Repository
public interface CategoryRepository  extends ReactiveMongoRepository<Category, String> {
    Mono<Category> findByName(String name);
}