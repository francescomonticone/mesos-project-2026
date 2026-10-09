package it.polimi.ingsw.Network.DTO;

import java.io.Serializable;
import java.util.List;


/**
 * Data Transfer Object representing a summary of a game lobby.
 * Used to display available matches to clients before they join.
 *
 * @param lobbyId           Unique identifier of the lobby.
 * @param creatorNickname   Nickname of the player who created the lobby.
 * @param currentPlayers    Number of players currently in the lobby.
 * @param maxPlayers        Target number of players needed to start the match.
 * @param connectedNicknames List of nicknames of players currently waiting in the lobby.
 */
public record LobbyDTO(
        int lobbyId,
        String creatorNickname,
        int currentPlayers,
        int maxPlayers,
        List<String> connectedNicknames
) implements Serializable {}
