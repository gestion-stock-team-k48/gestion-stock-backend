package cm.kfokam.stock.vente;

import cm.kfokam.stock.vente.model.Vente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface VenteRepository extends JpaRepository<Vente, Long> {

    boolean existsByCodeAndIdEntreprise(String code, Long idEntreprise);

    Optional<Vente> findByCodeAndIdEntreprise(String code, Long idEntreprise);

    Optional<Vente> findByIdAndIdEntreprise(Long id, Long idEntreprise);

    Page<Vente> findAllByIdEntreprise(Long idEntreprise, Pageable pageable);

    long countByCodeStartingWithAndIdEntreprise(String prefix, Long idEntreprise);
}