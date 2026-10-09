package it.polimi.ingsw.Network.RMI;

import it.polimi.ingsw.Network.DTO.LobbyDTO;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * The remote interface exposed by the Server for RMI clients.
 * <p>
 * This interface replaces the Command Pattern (DTOs)
 * used in Socket communication with direct business method invocations,
 * fully leveraging the true nature of Remote Method Invocation (RMI).
 * </p>
 */
public interface ServerRemote extends Remote {

    // SETUP & LOBBY

    /**
     * Authenticates the client and registers their remote callback.
     *
     * @param nickname     the requested username
     * @param passwordHash the hashed password
     * @param callback     the remote stub of the client
     * @return true if the login was successful, false otherwise
     * @throws RemoteException if a network error occurs
     */
    boolean login(String nickname, String passwordHash, ClientRemote callback) throws RemoteException;

    //NOTE: only on login we pass the clientCallback. Then it will be saved in the map and we can retrieve it by the nickname directly
    /**
     * Creates a new match lobby and automatically adds the creator to it.
     *
     * @param nickname   the username of the player creating the lobby
     * @param targetSize the desired number of players for the match
     * @throws RemoteException if a network error occurs during the remote call
     */
    void createLobby(String nickname, int targetSize) throws RemoteException;

    /**
     * Attempts to add the player to an existing lobby.
     *
     * @param nickname the username of the player attempting to join
     * @param lobbyId  the unique identifier of the lobby to join
     * @throws RemoteException if a network error occurs during the remote call
     */
    void joinLobby(String nickname, int lobbyId) throws RemoteException;

    /**
     * Retrieves the list of currently open lobbies with their status.
     *
     * @return a list of {@link LobbyDTO} containing lobby info.
     * @throws RemoteException if a network error occurs.
     */
    List<LobbyDTO> getOpenLobbies() throws RemoteException;


    // MATCH ACTIONS
    /**
     * Executes the player's action of placing their totem on a specific tile.
     *
     * @param nickname the username of the player performing the action
     * @param tileId   the identifier of the board tile where the totem is placed
     * @throws RemoteException if a network error occurs during the remote call
     */
    void placeTotem(String nickname, char tileId) throws RemoteException;

    /**
     * Executes the player's action of resolving an offer (selecting cards).
     *
     * @param nickname     the username of the player performing the action
     * @param upCards      the list of card identifiers selected from the face-up pool
     * @param downCards    the list of card identifiers selected from the face-down pool
     * @param orderedCards the ordered list of card identifiers indicating how the player arranges their selection
     * @throws RemoteException if a network error occurs during the remote call
     */
    void resolveOffer(String nickname, List<String> upCards, List<String> downCards, List<String> orderedCards) throws RemoteException;

    /**
     * Executes the player's action of picking an extra card during a special phase.
     *
     * @param nickname   the username of the player performing the action
     * @param cardId     the identifier of the selected extra card
     * @param isUpperRow a boolean indicating if the card was drawn from the upper row (true) or lower row (false)
     * @throws RemoteException if a network error occurs during the remote call
     */
    void pickExtraCard(String nickname, String cardId, boolean isUpperRow) throws RemoteException;

    /**
     * Executes the player's decision to skip picking an extra card.
     *
     * @param nickname the username of the player skipping the action
     * @throws RemoteException if a network error occurs during the remote call
     */
    void skipExtra(String nickname) throws RemoteException;

}