package it.polimi.ingsw.Network.DTO;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO representing a single entry in the endgame leaderboard.
 * This class is also used to save the score as an ordered list
 * and is used from ResultDAO class in the getLeaderboard method
 *
 */
public record MatchResultDTO(
        String nickname,
        int prestigePoints,
        int food,
        Date date
) implements Serializable {}