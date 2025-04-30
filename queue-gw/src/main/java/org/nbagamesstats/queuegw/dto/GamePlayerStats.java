package org.nbagamesstats.queuegw.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GamePlayerStats {
    @NotEmpty(message = "season can't be empty")
    private String season;
    @NotEmpty(message = "game can't be empty")
    private String game;
    @NotEmpty(message = "team can't be empty")
    private String team;
    @NotEmpty(message = "player name can't be empty")
    private String playerName;
    @PositiveOrZero(message = "points can't be negative")
    private int points;
    @PositiveOrZero(message = "rebounds can't be negative")
    private int rebounds;
    @PositiveOrZero(message = "assists can't be negative")
    private int assists;
    @PositiveOrZero(message = "steals can't be negative")
    private int steals;
    @PositiveOrZero(message = "blocks can't be negative")
    private int blocks;
    // integer, max value: 6
    @PositiveOrZero(message = "fouls can't be negative")
    @Max(value = 6, message = "fouls can't be can't be greater than 6")
    private int fouls;
    // float between 0 and 48.0
    @PositiveOrZero(message = "playedMinutes can't be negative")
    @DecimalMax(value = "48.0", message = "playedMinutes can't be greater than 48.0")
    private float playedMinutes;
}
