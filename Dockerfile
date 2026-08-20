# syntax=docker/dockerfile:1.7
#
# Image du backend, en trois étages.
#
# `deps` télécharge les dépendances : tant que le `pom.xml` ne bouge pas, il est repris du
# cache et le build ne retouche pas au réseau. `build` compile et éclate le jar. L'étage final
# ne reçoit qu'un JRE et le jar éclaté — ni Maven, ni JDK, ni sources.
#
# Le jar est éclaté en couches Spring Boot plutôt que copié tel quel : les dépendances, qui
# pèsent l'essentiel et ne changent presque jamais, forment une couche stable que Docker
# réutilise d'un déploiement à l'autre. Seule la couche applicative, un méga-octet et demi,
# est repoussée à chaque commit.
#
# Un runtime `jlink` sur mesure a été essayé puis retiré : mesuré, il rendait 142 Mo contre
# 141 Mo pour le JRE distribué. Eclipse Temurin taille déjà son image alpine au plus près, et
# une liste de modules réduite se briserait sur le chargement par réflexion de Spring.

# ── Dépendances ──────────────────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS deps
WORKDIR /chantier
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline

# ── Compilation ──────────────────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /chantier
COPY pom.xml .
COPY src ./src
# Les tests tournent dans la CI, pas ici : les rejouer à chaque build d'image doublerait la
# durée sans rien vérifier de plus, et l'image ne se publie qu'après un pipeline vert.
RUN --mount=type=cache,target=/root/.m2 mvn -B -q clean package -DskipTests
RUN java -Djarmode=layertools -jar target/*.jar extract --destination /couches

# ── Exécution ────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# `apk upgrade` d'abord : entre deux publications de l'image Temurin, Alpine corrige ses
# paquets, et c'est là que dorment les failles qu'un scanner remonte. C'est sur cette ligne
# que le pipeline a buté la première fois, sur un openssl en retard de quatre correctifs.
#
# `wget` vient de busybox, déjà présent. L'utilisateur n'est pas root : un processus
# applicatif n'a aucune raison de l'être, et une évasion depuis le conteneur ne donnerait
# alors rien d'intéressant.
RUN apk upgrade --no-cache \
    && addgroup -S stock && adduser -S -G stock -H -s /sbin/nologin stock

WORKDIR /application

COPY --from=build --chown=stock:stock /couches/dependencies/ ./
COPY --from=build --chown=stock:stock /couches/spring-boot-loader/ ./
COPY --from=build --chown=stock:stock /couches/snapshot-dependencies/ ./
COPY --from=build --chown=stock:stock /couches/application/ ./

USER stock

EXPOSE 8080

# `MaxRAMPercentage` plutôt qu'un `-Xmx` fixe : la JVM lit alors la limite mémoire du
# conteneur, et la même image tient dans 512 Mo comme dans 4 Go sans être reconstruite.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Djava.security.egd=file:/dev/./urandom"
ENV SERVER_PORT=8080

# La sonde lit le groupe `readiness` — l'application est-elle en état de servir — et non
# l'agrégat, qui tomberait avec le premier service annexe indisponible.
HEALTHCHECK --interval=15s --timeout=3s --start-period=60s --retries=5 \
    CMD wget -qO- "http://127.0.0.1:${SERVER_PORT}/api/v1/actuator/health/readiness" | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
