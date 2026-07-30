package cm.kfokam.stock.commandeclient.model;

import cm.kfokam.stock.article.model.Article;
import cm.kfokam.stock.common.entity.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "lignes_commande_client")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class LigneCommandeClient extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_client_id", nullable = false)
    private CommandeClient commandeClient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Column(nullable = false)
    private Integer quantite;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal prixUnitaireHt;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal prixUnitaireTtc;
}