package it.polimi.ingsw.Network.DTO;

import java.io.Serializable;
import java.util.List;

/**
 * Data Transfer Object representing the public state of the game board.
 * It contains the IDs of the cards currently available for drawing and
 * the current status of the Offer Track.
 *
 * @param upperRowCardIds list of unique IDs for the cards in the upper row
 * @param lowerRowCardIds list of unique IDs for the cards in the lower row
 * @param offerTrackDTO      list of DTOs representing the offer track and totem placements
 * @param turnOrderTileDTO   DTO representing the turn order tile and its occupants
 */
public record BoardDTO(
        List<String> upperRowCardIds,
        List<String> lowerRowCardIds,
        List<OfferTileDTO> offerTrackDTO,
        TurnOrderTileDTO turnOrderTileDTO
) implements Serializable {}