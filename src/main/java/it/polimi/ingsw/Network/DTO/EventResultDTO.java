package it.polimi.ingsw.Network.DTO;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents the overall result of resolving a single Event card.
 *
 * @param eventCardId  the unique identifier of the resolved Event card
 * @param playerDeltas a map linking each player's nickname to their respective stat changes
 */
public record EventResultDTO(
        String eventCardId,
        Map<String, PlayerDeltaDTO> playerDeltas
) implements Serializable {}