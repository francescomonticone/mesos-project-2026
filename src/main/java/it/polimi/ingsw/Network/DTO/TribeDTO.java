package it.polimi.ingsw.Network.DTO;

import java.io.Serializable;

/**
 * Data transfer object containing the computed statistics of a tribe.
 *
 * @param hunterCount the number of hunters
 * @param shamanCount the number of shamans
 * @param shamanStarsCount the number of shaman stars
 * @param artistCount the number of artists
 * @param builderCount the number of builders
 * @param builderDiscount the building discount
 * @param gathererCount the number of gatherers
 * @param inventorCount the number of inventors
 * @param distinctInvention the number of distinct inventions
 * @param inventionPair the number of equal invention pairs
 * @param sustenanceDiscount the sustenance discount
 */
public record TribeDTO(
        int hunterCount,
        int shamanCount,
        int shamanStarsCount,
        int artistCount,
        int builderCount,
        int builderDiscount,
        int gathererCount,
        int inventorCount,
        int distinctInvention,
        int inventionPair,
        int sustenanceDiscount
)implements Serializable {}
