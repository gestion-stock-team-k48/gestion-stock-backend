package cm.kfokam.stock.entreprise;

import cm.kfokam.stock.entreprise.model.Entreprise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface EntrepriseRepository extends JpaRepository<Entreprise, Long> {

    boolean existsByCodeFiscal(String codeFiscal);

    Optional<Entreprise> findByCodeFiscal(String codeFiscal);
}