package it.polimi.ingsw.Network.RMI;

import it.polimi.ingsw.Network.DTO.LobbyDTO;
import it.polimi.ingsw.Network.DTO.MatchDTO;
import it.polimi.ingsw.Network.DTO.MatchResultDTO;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * The remote interface exposed by the Client for the Server to call.
 * <p>
 * The Server uses this stub to invoke UI updates directly on the Client's machine.
 * </p>
 */
public interface ClientRemote extends Remote {

    /**
     * Called periodically by the server to verify if the client is still connected.
     * If this method throws a RemoteException, the server assumes the client crashed or disconnected.
     *
     * @throws RemoteException if the client is unreachable or a network error occurs
     */
    void ping() throws RemoteException;

    /**
     * Sends the updated game state to the client.
     * @param snapshot the lightweight DTO representing the current state of the board and match
     * @throws RemoteException if a communication error occurs during the remote call
     */
    void updateModel(MatchDTO snapshot) throws RemoteException;

    /**
     * Displays an error message on the client's screen.
     * * @param message the text of the error to display
     * @throws RemoteException if a communication error occurs during the remote call
     */
    void showError(String message) throws RemoteException;

    /**
     * Displays a generic informational message on the client's screen.
     * * @param message the text of the message to display
     * @throws RemoteException if a communication error occurs during the remote call
     */
    void showMessage(String message) throws RemoteException;

    /**
     * Prompts the client to place their totem on the board.
     * This is triggered by the server when it is the player's turn to perform the totem placement phase.
     * * @throws RemoteException if a communication error occurs during the remote call
     */
    void askPlaceTotem() throws RemoteException;

    /**
     * Prompts the client to resolve an offer.
     * This is triggered by the server when the player needs to choose cards or resources from the available pool.
     * * @throws RemoteException if a communication error occurs during the remote call
     */
    void askResolveOffer() throws RemoteException;

    /**
     * Prompts the client to select or draw an extra card.
     * This is typically triggered by specific game events, rules, or card effects.
     * * @throws RemoteException if a communication error occurs during the remote call
     */
    void askExtraCard() throws RemoteException;

    /**
     * Called by the server to display the list of available lobbies to the client.
     * * @param lobbies a list of {@link LobbyDTO} representing the currently open and joinable lobbies
     * @throws RemoteException if a communication error occurs during the remote call
     */
    void showOpenLobbies(List<LobbyDTO> lobbies) throws RemoteException;

    /**
     * Called by the server to display the end-game results.
     * Includes both the final ranking for the current match and the updated global leaderboard.
     * * @param matchRanking a list of {@link MatchResultDTO} representing the standings of the just-finished match
     * @param globalLeaderboard a list of {@link MatchResultDTO} representing the all-time high scores from the database
     * @param numPlayers the total number of players in the match
     * @throws RemoteException if a communication error occurs during the remote call
     */
    void endGame(List<MatchResultDTO> matchRanking, List<MatchResultDTO> globalLeaderboard, int numPlayers) throws RemoteException;
}