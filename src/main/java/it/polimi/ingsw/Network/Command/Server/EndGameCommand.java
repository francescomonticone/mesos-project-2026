package it.polimi.ingsw.Network.Command.Server;

import it.polimi.ingsw.Client.ClientController.ClientController;
import it.polimi.ingsw.Network.DTO.MatchResultDTO;

import java.io.Serial;
import java.util.List;

//Command Server -> Client

/**
 * A network command sent from the server to the client, signaling the conclusion of the match
 * and delivering the final results.
 * <p>
 * Following the Command Pattern, this class represents a server-initiated broadcast.
 * It encapsulates all the necessary end-game data, including the local standings of the
 * current match and the historical global leaderboard. Upon reception, the client executes
 * this command to transition the user interface to the final score screen.
 * </p>
 */
public class EndGameCommand implements ServerCommand {

    @Serial
    private static final long serialVersionUID = 1L;
    private final List<MatchResultDTO> matchRanking;
    private final List<MatchResultDTO> globalLeaderboard;
    private final int numPlayers;

    /**
     * Constructs a new command to finalize the game and display the leaderboards.
     *
     * @param matchRanking      the final ranking of the players in the completed match
     * @param globalLeaderboard the overall top scores across all played matches on the server
     * @param numPlayers        the total number of players who played in this specific match
     */
    public EndGameCommand(List<MatchResultDTO> matchRanking, List<MatchResultDTO> globalLeaderboard, int numPlayers) {
        this.matchRanking = matchRanking;
        this.globalLeaderboard = globalLeaderboard;
        this.numPlayers = numPlayers;
    }

    /**
     * Executes the command on the client side.
     * <p>
     * This method is invoked by the client's network listener once the command object
     * is fully received from the server. It delegates the transition to the end-game UI
     * to the {@link ClientController}, passing along the rankings and player count data.
     * </p>
     *
     * @param controller the main client-side controller responsible for managing the game state and UI
     */
    @Override
    public void executeOnClient(ClientController controller) {
        controller.onEndGame(matchRanking, globalLeaderboard, numPlayers);
    }
}