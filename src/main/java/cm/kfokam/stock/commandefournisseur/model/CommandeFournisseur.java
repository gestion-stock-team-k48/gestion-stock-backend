package cm.kfokam.stock.commandefournisseur.model;

import cm.kfokam.stock.entreprise.model.Entreprise;
import cm.kfokam.stock.fournisseur.model.Fournisseur;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "commandes_fournisseur", uniqueConstraints = @UniqueConstraint(columnNames = {"code_commande", "entreprise_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandeFournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String codeCommande;

    @Column(nullable = false)
    private LocalDate dateCommande;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EtatCommande etatCommande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id", nullable = false)
    private Fournisseur fournisseur;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalHt;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalTva;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalTtc;

    @Builder.Default
    @OneToMany(mappedBy = "commandeFournisseur", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LigneCommandeFournisseur> lignes = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;
}