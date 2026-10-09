package it.polimi.ingsw.Network.DTO;

import java.io.Serializable;

/**
 * Data Transfer Object representing a single tile on the Offer Track.
 * It contains the public information needed by the client to render the tile
 * and show if a player has placed their totem on it.
 *
 * @param tileId           the unique identifier of the offer tile (e.g., 'A', 'B', 'C')
 * @param occupantNickname the nickname of the player who placed their totem here, or {@code null} if empty
 */
public record OfferTileDTO(
        char tileId,
        String occupantNickname
) implements Serializable {}