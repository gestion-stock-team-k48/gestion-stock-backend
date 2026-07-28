package cm.kfokam.stock.article.model;

import cm.kfokam.stock.category.model.Category;
import cm.kfokam.stock.entreprise.model.Entreprise;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "articles", uniqueConstraints = @UniqueConstraint(columnNames = {"code", "entreprise_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 255)
    private String designation;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal prixUnitaireHt;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxTva;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal prixUnitaireTtc;

    private String photo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal seuilMinimum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;
}