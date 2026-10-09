package it.polimi.ingsw.Network.DTO;

import java.io.Serializable;
import java.util.List;

/**
 * Data Transfer Object representing the comprehensive public state of a match.
 * <p>
 * This record serves as the root snapshot broadcasted from the server to the clients,
 * containing all the necessary information to render the current game state on the UI.
 * It also encapsulates the synchronization data for handling player disconnections.
 * Note that the game state transitions are handled polymorphically by the server's state machine.
 * </p>
 *
 * @param matchId                    the unique identifier of the match
 * @param currentRound               the current round number (e.g., 1 to 10)
 * @param currentEra                 the current era of the game
 * @param currentPlayer              the nickname of the player whose turn it is ({@code null} if the game is in an automatic resolution state)
 * @param board                      the {@link BoardDTO} encapsulating the shared board state (cards, offer track, turn order)
 * @param players                    the list of {@link PlayerDTO} representing the public state of all participating players
 * @param currentPhaseName           the name of the current game phase, used by the client to activate the correct action panels
 * @param totalDisconnectionTime     the total duration, in seconds, allocated for the disconnection timeout
 * @param remainingDisconnectionTime the exact number of seconds remaining before the lone player wins by abandonment
 * @param eventResults               the result of the most recent event, if any, used to trigger animations on the client side
 */

public record MatchDTO(
        int matchId,
        int currentRound,
        int currentEra,
        String currentPlayer,
        BoardDTO board,
        List<PlayerDTO> players,
        String currentPhaseName,
        int totalDisconnectionTime,
        int remainingDisconnectionTime,
        List<EventResultDTO> eventResults
) implements Serializable {}