package cm.kfokam.stock.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(

        @NotBlank(message = "Le code est obligatoire")
        @Size(max = 20, message = "Le code ne doit pas dépasser 20 caractères")
        String code,

        @NotBlank(message = "La désignation est obligatoire")
        @Size(max = 255, message = "La désignation ne doit pas dépasser 255 caractères")
        String designation
) {
}
