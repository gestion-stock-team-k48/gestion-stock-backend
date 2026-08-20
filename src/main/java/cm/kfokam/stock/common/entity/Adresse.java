package cm.kfokam.stock.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Adresse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "adresse1", length = 255)
    private String adresse1;

    @Column(name = "adresse2", length = 255)
    private String adresse2;

    @Column(name = "ville", length = 100)
    private String ville;

    @Column(name = "pays", length = 100)
    private String pays;

    @Column(name = "code_postal", length = 20)
    private String codePostal;
}