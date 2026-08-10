package cm.kfokam.stock.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Vérifie que {@link JacksonConfig} sérialise/désérialise les types java.time
 * au format ISO-8601 attendu, sans timestamps numériques.
 */
class JacksonConfigTest {

    private final ObjectMapper objectMapper = build();

    private ObjectMapper build() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        new JacksonConfig().jacksonObjectMapperCustomizer().customize(builder);
        return builder.build();
    }

    @Test
    void serializesInstantAsIsoUtcString() throws Exception {
        Instant instant = Instant.parse("2026-08-10T10:39:00Z");

        String json = objectMapper.writeValueAsString(instant);

        assertThat(json).isEqualTo("\"2026-08-10T10:39:00Z\"");
    }

    @Test
    void serializesLocalDateTimeWithoutTimestamp() throws Exception {
        LocalDateTime dateTime = LocalDateTime.of(2026, 8, 10, 10, 39, 0);

        String json = objectMapper.writeValueAsString(dateTime);

        assertThat(json).isEqualTo("\"2026-08-10T10:39:00\"");
    }

    @Test
    void serializesLocalDateAsIsoDate() throws Exception {
        LocalDate date = LocalDate.of(2026, 8, 10);

        String json = objectMapper.writeValueAsString(date);

        assertThat(json).isEqualTo("\"2026-08-10\"");
    }

    @Test
    void roundTripsInstantFromIsoString() throws Exception {
        Instant instant = Instant.parse("2026-08-10T10:39:00Z");

        String json = objectMapper.writeValueAsString(instant);
        Instant parsed = objectMapper.readValue(json, Instant.class);

        assertThat(parsed).isEqualTo(instant);
    }

    @Test
    void roundTripsLocalDateTimeFromIsoString() throws Exception {
        LocalDateTime dateTime = LocalDateTime.of(2026, 8, 10, 10, 39, 0);

        String json = objectMapper.writeValueAsString(dateTime);
        LocalDateTime parsed = objectMapper.readValue(json, LocalDateTime.class);

        assertThat(parsed).isEqualTo(dateTime);
    }

    @Test
    void roundTripsLocalDateFromIsoString() throws Exception {
        LocalDate date = LocalDate.of(2026, 8, 10);

        String json = objectMapper.writeValueAsString(date);
        LocalDate parsed = objectMapper.readValue(json, LocalDate.class);

        assertThat(parsed).isEqualTo(date);
    }
}