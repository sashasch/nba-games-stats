package org.nbagamesstats.queuegw.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nbagamesstats.queuegw.dto.GamePlayerStats;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseAccessLayer {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Upsert gamePlayerStats
     * @param gamePlayerStats - Game player statistics
     */
    public void upsertGamePlayerStats(GamePlayerStats gamePlayerStats) {
        try {
            jdbcTemplate.execute((Connection con) -> {
                CallableStatement cs = con.prepareCall("CALL upsert_game_player_stats(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

                cs.setString(1, gamePlayerStats.getSeason());
                cs.setString(2, gamePlayerStats.getGame());
                cs.setString(3, gamePlayerStats.getTeam());
                cs.setString(4, gamePlayerStats.getPlayerName());
                cs.setInt(5, gamePlayerStats.getPoints());
                cs.setInt(6, gamePlayerStats.getRebounds());
                cs.setInt(7, gamePlayerStats.getAssists());
                cs.setInt(8, gamePlayerStats.getSteals());
                cs.setInt(9, gamePlayerStats.getBlocks());
                cs.setInt(10, gamePlayerStats.getFouls());
                cs.setBigDecimal(11, BigDecimal.valueOf(gamePlayerStats.getPlayedMinutes())); // Use BigDecimal for NUMERIC

                cs.execute();
                return null;
            });
            log.debug("Stored procedure upsert_game_player_stats executed successfully!");
        } catch (DataAccessException ex) {
            log.error("Failed to execute stored procedure: " + ex.getMessage(), ex);
            throw new RuntimeException("Failed to execute upsert_game_player_stats: " + ex.getMessage(), ex);
        }
    }

    /**
     * Updates season stats report for the player
     * @param gamePlayerStats - Game player statistics
     */
    public void updateSeasonPlayerStatsReport(GamePlayerStats gamePlayerStats){
        try {
            jdbcTemplate.execute((Connection con) -> {
                CallableStatement cs = con.prepareCall("CALL refresh_season_player_stats_report(?, ?, ?)");
                cs.setString(1, gamePlayerStats.getSeason());
                cs.setString(2, gamePlayerStats.getTeam());
                cs.setString(3, gamePlayerStats.getPlayerName());

                cs.execute();
                return null;
            });
            log.debug("Stored procedure refresh_season_player_stats_report executed successfully!");
        } catch (DataAccessException ex) {
            log.error("Failed to execute stored procedure: " + ex.getMessage(), ex);
            throw new RuntimeException("Failed to execute refresh_season_player_stats_report: " + ex.getMessage(), ex);
        }
    }
}
