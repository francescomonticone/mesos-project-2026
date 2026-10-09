package it.polimi.ingsw.Server.Model.Cards.CharacterCards;

import it.polimi.ingsw.Server.Model.Match.Era;

/**
 * Represents a Builder character card in the game.
 * <p>
 * Builders are essential for the engine-building aspect of a player's Tribe.
 * They provide a permanent discount on the food cost required to construct
 * {@link it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingCard}s.
 * Furthermore, they grant base prestige points that are calculated at the end of the game,
 * which can be significantly increased by specific building multiplier effects.
 * </p>
 */
public class Builder extends CharacterCard{
    private final int buildingDiscount;
    private final int prestigePoints;

    public Builder(int minPlayers, Era era, String id, int buildingDiscount, int prestigePoints, String type) {
        super(minPlayers, era, id, type);
        this.buildingDiscount = buildingDiscount;
        this.prestigePoints = prestigePoints;
    }

    /**
     * Retrieves the base prestige points provided by this builder.
     * <p>
     * Used by the tribe during the end-game scoring phase, where these points
     * might be enhanced by specific multiplier effects from buildings.
     * </p>
     *
     * @return the base prestige points
     */
    @Override
    public int getBuilderPoints(){return this.prestigePoints;}

    /**
     * Retrieves the food discount granted by this builder.
     * <p>
     * This value reduces the total food cost whenever the player attempts to
     * construct a new building card.
     * </p>
     *
     * @return the food discount value
     */
    @Override
    public int getBuildingDiscount(){
        return this.buildingDiscount;
    }

    /**
     * Retrieves the specific string classification of this character.
     * <p>
     * Returning exactly "BUILDER" is crucial for the game engine to correctly track
     * distinct character classes when computing bonuses for complete character sets.
     * </p>
     *
     * @return the string literal representing the character type
     */
    @Override
    public String getType(){
        return type; //this will be BUILDER
    }
}
