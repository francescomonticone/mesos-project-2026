package it.polimi.ingsw.Network.DTO;

import java.io.Serializable;
import java.util.List;

/**
 * Data Transfer Object representing the Turn Order Tile.
 * It tracks the order of players for the next round based on where
 * they placed their totems during the resolution phase.
 *
 * @param playerTopToBottom ordered list of player nicknames currently on the tile.
 * Index 0 is the first position, index 1 is the second, etc.
 * Null values can be used to represent empty spaces if needed.
 */
public record TurnOrderTileDTO(
        List<String> playerTopToBottom
) implements Serializable {}