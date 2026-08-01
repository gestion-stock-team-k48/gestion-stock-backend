package cm.kfokam.stock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class GestionStockApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionStockApplication.class, args);
	}

}
