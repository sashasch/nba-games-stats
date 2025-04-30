package org.nbagamesstats.queuegw.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nbagamesstats.queuegw.dal.DatabaseAccessLayer;
import org.nbagamesstats.queuegw.dto.GamePlayerStats;
import org.springframework.kafka.core.KafkaTemplate;
import java.util.Collections;
import java.util.Set;

import static org.mockito.Mockito.*;

public class GamePlayerStatsConsumerTest {

    private KafkaTemplate<String, String> kafkaTemplate;
    private ObjectMapper objectMapper;
    private Validator validator;
    private DatabaseAccessLayer databaseAccessLayer;

    private GamePlayerStatsConsumer consumer;

    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        objectMapper = new ObjectMapper();
        validator = mock(Validator.class);
        databaseAccessLayer = mock(DatabaseAccessLayer.class);

        consumer = new GamePlayerStatsConsumer(kafkaTemplate, objectMapper, validator, databaseAccessLayer);
        consumer.setDeadLetterTopic("dead-letter-topic");
    }

    @Test
    void shouldProcessValidMessage() throws Exception {
        GamePlayerStats stats = new GamePlayerStats("2024-2025", "GAME12345", "LAL", "LeBron James", 27, 8, 9, 2, 1, 3, 35.5f);
        String json = objectMapper.writeValueAsString(stats);

        when(validator.validate(any())).thenReturn(Collections.emptySet());

        consumer.handleGamePlayerStats(json);

        verify(databaseAccessLayer).upsertGamePlayerStats(any());
        verify(databaseAccessLayer).updateSeasonPlayerStatsReport(any());
        verify(kafkaTemplate, never()).send(eq("dead-letter-topic"), anyString());
    }

    @Test
    void shouldSendToDeadLetterOnValidationFailure() throws Exception {
        GamePlayerStats stats = new GamePlayerStats("2024-2025", "GAME12345", "LAL", "LeBron James", 27, 8, 9, 2, 1, 3, 35.5f);
        String json = objectMapper.writeValueAsString(stats);

        ConstraintViolation<GamePlayerStats> violation = mock(ConstraintViolation.class);
        Set<ConstraintViolation<GamePlayerStats>> violations = Set.of(violation);

        when(validator.validate(any(GamePlayerStats.class))).thenReturn(violations);

        consumer.handleGamePlayerStats(json);

        verify(kafkaTemplate).send(eq("dead-letter-topic"), contains("Validation failed"));
        verify(databaseAccessLayer, never()).upsertGamePlayerStats(any());
    }

    @Test
    void shouldSendToDeadLetterOnJsonParseError() {
        String badJson = "{ invalid json }";

        consumer.handleGamePlayerStats(badJson);

        verify(kafkaTemplate).send(eq("dead-letter-topic"), contains("Unexpected character"));
    }
}
