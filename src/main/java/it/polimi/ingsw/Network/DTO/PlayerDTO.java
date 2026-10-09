package it.polimi.ingsw.Network.DTO;

import java.io.Serializable;
import java.util.List;

/**
 * Data Transfer Object representing the public state of a single player.
 * It provides the client with all the necessary information to render
 * the player's personal board, resources, and collected cards.
 *
 * @param nickname          the player's unique nickname
 * @param foodTokens        the current amount of food tokens owned
 * @param prestigePoints    the current amount of prestige points earned
 * @param totemColor        the string representation of the player's totem color
 * @param isConnected       {@code true} if the player is currently connected to the server
 */
public record PlayerDTO(
        String nickname,
        int foodTokens,
        int prestigePoints,
        String totemColor,
        boolean isConnected,
        List<String> ownedCharacterIds,
        List<String> ownedBuildingIds,
        TribeDTO tribeDTO
) implements Serializable {}