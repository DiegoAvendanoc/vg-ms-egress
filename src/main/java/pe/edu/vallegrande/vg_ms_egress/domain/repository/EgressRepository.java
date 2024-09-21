package pe.edu.vallegrande.vg_ms_egress.domain.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vg_ms_egress.domain.model.Egress;

@Repository
public interface EgressRepository extends ReactiveMongoRepository<Egress, String> {

}