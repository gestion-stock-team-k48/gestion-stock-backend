package cm.kfokam.stock.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Configuration globale de sérialisation JSON (Jackson).
 * <p>
 * Objectif : garantir que toutes les dates exposées/consommées par l'API respectent
 * un format ISO-8601 lisible plutôt que des timestamps numériques (millisecondes),
 * indépendamment du type java.time utilisé dans les DTOs.
 * <ul>
 *     <li>{@link Instant}       -&gt; {@code 2026-08-10T10:39:00Z} (UTC)</li>
 *     <li>{@link LocalDateTime} -&gt; {@code 2026-08-10T10:39:00}</li>
 *     <li>{@link LocalDate}     -&gt; {@code 2026-08-10}</li>
 * </ul>
 */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter INSTANT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);

    private static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private static final DateTimeFormatter LOCAL_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonObjectMapperCustomizer() {
        return builder -> {
            // Dates lisibles (ISO-8601) plutôt que des timestamps epoch en millisecondes.
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            JavaTimeModule javaTimeModule = new JavaTimeModule();
            javaTimeModule.addSerializer(Instant.class, new InstantIsoUtcSerializer());
            javaTimeModule.addDeserializer(Instant.class,
                    InstantDeserializer.INSTANT);

            javaTimeModule.addSerializer(LocalDateTime.class,
                    new LocalDateTimeSerializer(LOCAL_DATE_TIME_FORMATTER));
            javaTimeModule.addDeserializer(LocalDateTime.class,
                    new LocalDateTimeDeserializer(LOCAL_DATE_TIME_FORMATTER));

            javaTimeModule.addSerializer(LocalDate.class,
                    new LocalDateSerializer(LOCAL_DATE_FORMATTER));
            javaTimeModule.addDeserializer(LocalDate.class,
                    new LocalDateDeserializer(LOCAL_DATE_FORMATTER));

            builder.modules(javaTimeModule);
        };
    }

    /**
     * Sérialise un {@link Instant} au format ISO-8601 UTC (ex: {@code 2026-08-10T10:39:00Z}).
     * <p>
     * Écrit à la main plutôt que via {@code InstantSerializer} de jackson-datatype-jsr310 :
     * son constructeur permettant un {@link DateTimeFormatter} personnalisé est {@code protected}.
     */
    private static final class InstantIsoUtcSerializer extends JsonSerializer<Instant> {
        @Override
        public void serialize(Instant value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(INSTANT_FORMATTER.format(value));
        }
    }
}
