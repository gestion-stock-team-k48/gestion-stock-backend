package cm.kfokam.stock.entreprise.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Adresse {

    @Column(length = 150)
    private String rue;

    @Column(length = 100)
    private String ville;

    @Column(length = 20)
    private String codePostal;

    @Column(length = 100)
    private String pays;
}