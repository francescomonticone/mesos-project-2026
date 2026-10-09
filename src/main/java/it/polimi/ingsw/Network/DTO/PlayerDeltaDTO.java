package it.polimi.ingsw.Network.DTO;

import java.io.Serializable;

/**
 * Represents the changes applied to a single player during the resolution of an event.
 *
 * @param foodChange               the amount of food gained (positive value) or lost (negative value)
 * @param pointsChange             the amount of prestige points gained (positive value) or lost (negative value)
 */
public record PlayerDeltaDTO(
        int foodChange,
        int pointsChange
) implements Serializable {}