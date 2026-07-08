package cm.kfokam.stock.client;

import cm.kfokam.stock.client.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

interface ClientRepository extends JpaRepository<Client, Long> {
}
