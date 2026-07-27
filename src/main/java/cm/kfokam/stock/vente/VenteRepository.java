package cm.kfokam.stock.vente;

import cm.kfokam.stock.vente.model.Vente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface VenteRepository extends JpaRepository<Vente, Long> {

    boolean existsByCode(String code);

    Optional<Vente> findByCode(String code);

    long countByCodeStartingWith(String prefix);
}