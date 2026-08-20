package cm.kfokam.stock.entreprise.model;

import cm.kfokam.stock.common.entity.AbstractEntity;
import cm.kfokam.stock.common.entity.Adresse;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "entreprises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Entreprise extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(length = 1000)
    private String description;

    @Embedded
    private Adresse adresse;

    @Column(nullable = false, unique = true, length = 30)
    private String codeFiscal;

    private String photo;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 20)
    private String numTel;

    @Column(length = 150)
    private String siteWeb;
}