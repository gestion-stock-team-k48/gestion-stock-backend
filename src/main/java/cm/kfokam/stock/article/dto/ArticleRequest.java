package cm.kfokam.stock.article.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ArticleRequest(

        @NotBlank(message = "Le code est obligatoire")
        @Size(max = 20, message = "Le code ne doit pas dépasser 20 caractères")
        String code,

        @NotBlank(message = "La désignation est obligatoire")
        @Size(max = 255, message = "La désignation ne doit pas dépasser 255 caractères")
        String designation,

        @NotNull(message = "Le prix unitaire HT est obligatoire")
        @Positive(message = "Le prix unitaire HT doit être positif")
        BigDecimal prixUnitaireHt,

        @NotNull(message = "Le taux de TVA est obligatoire")
        @PositiveOrZero(message = "Le taux de TVA doit être positif ou nul")
        BigDecimal tauxTva,

        @NotNull(message = "Le prix unitaire TTC est obligatoire")
        @Positive(message = "Le prix unitaire TTC doit être positif")
        BigDecimal prixUnitaireTtc,

        String photo,

        @NotNull(message = "Le seuil minimal est obligatoire")
        @PositiveOrZero(message = "Le seuil minimal doit être positif ou nul")
        BigDecimal seuilMinimum,

        @NotNull(message = "La catégorie est obligatoire")
        Long categoryId
) {
}