package cm.kfokam.stock.client;

import cm.kfokam.stock.client.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface ClientRepository extends JpaRepository<Client, Long> {

    boolean existsByEmailAndEntrepriseId(String email, Long entrepriseId);

    Optional<Client> findByEmailAndEntrepriseId(String email, Long entrepriseId);

    Optional<Client> findByIdAndEntrepriseId(Long id, Long entrepriseId);

    List<Client> findAllByEntrepriseId(Long entrepriseId);
}