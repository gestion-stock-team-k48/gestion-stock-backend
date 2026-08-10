package cm.kfokam.stock.common.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Socle commun d'audit JPA pour toutes les entités métier.
 * <p>
 * {@link AuditingEntityListener} (déclenché par {@code @EnableJpaAuditing}, voir
 * {@code JpaAuditingConfig}) remplit automatiquement ces quatre champs à la persistance
 * et à la mise à jour ; {@code ApplicationAuditorAware} fournit l'identifiant de
 * l'utilisateur courant (email, ou {@code "SYSTEM"} en l'absence d'authentification).
 * <p>
 * Champs en lecture seule côté API : documentés {@code READ_ONLY} pour Swagger/OpenAPI afin
 * qu'ils apparaissent dans les réponses sans jamais être exigés dans un corps de requête.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Date de création", example = "2026-08-10T11:30:13")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Date de dernière modification", example = "2026-08-10T13:36:32")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Identifiant du créateur")
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Identifiant du dernier modificateur")
    private String updatedBy;
}
