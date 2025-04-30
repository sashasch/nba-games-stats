package org.nbagamesstats.queuegw.consumer;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.nbagamesstats.queuegw.dal.DatabaseAccessLayer;
import org.nbagamesstats.queuegw.dto.GamePlayerStats;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Set;

@Service
@Getter
@Setter
@RequiredArgsConstructor
@Slf4j
public class GamePlayerStatsConsumer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    private final Validator validator;

    private final DatabaseAccessLayer databaseAccessLayer;

    @Value("${kafka.topic.dead-letter-topic}")
    private String deadLetterTopic;

    /**
     * Consumes message from Kafka broker, converts to expected format, validates and stores to DB.
     * On any error the failure message and the original one will be published to DeadLetterQueue
     * @param message game-player-stats-topic message, expected valid json convertible to GamePlayerStats.
     */
    @KafkaListener(topics = "${kafka.topic.game-player-stats-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleGamePlayerStats(@Payload String message) {
        try {
            GamePlayerStats gamePlayerStats = objectMapper.readValue(message, GamePlayerStats.class);
            log.debug("Arrived GamePlayerStats: {}", message);
            validate(gamePlayerStats);
            databaseAccessLayer.upsertGamePlayerStats(gamePlayerStats);
            databaseAccessLayer.updateSeasonPlayerStatsReport(gamePlayerStats);
            log.debug("GamePlayerStats for season {}, game {}, team {}, player {} stored successfully", gamePlayerStats.getSeason(), gamePlayerStats.getGame(), gamePlayerStats.getTeam(), gamePlayerStats.getPlayerName());
        } catch (Exception e) {
            sendToDeadLetterQueue(String.format("Cause : %s, failed message : %s", e.getMessage(), message));
        }
    }

    private void sendToDeadLetterQueue(String message) {
        kafkaTemplate.send(deadLetterTopic, message);
    }

    private void validate(GamePlayerStats emailProcessingDto) {
        Set<ConstraintViolation<GamePlayerStats>> violations = validator.validate(emailProcessingDto);
        if (violations != null && !violations.isEmpty()) {
            throw new ConstraintViolationException("Validation failed", violations);
        }
    }
}
