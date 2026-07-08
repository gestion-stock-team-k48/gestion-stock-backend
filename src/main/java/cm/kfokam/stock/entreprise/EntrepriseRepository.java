package cm.kfokam.stock.entreprise;

import cm.kfokam.stock.entreprise.model.Entreprise;
import org.springframework.data.jpa.repository.JpaRepository;

interface EntrepriseRepository extends JpaRepository<Entreprise, Long> {
}
