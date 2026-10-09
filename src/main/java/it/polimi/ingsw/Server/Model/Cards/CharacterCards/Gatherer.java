package it.polimi.ingsw.Server.Model.Cards.CharacterCards;

import it.polimi.ingsw.Server.Model.Match.Era;

/**
 * Represents a Gatherer character card in the game.
 * <p>
 * Gatherers are crucial for managing a player's resource economy. They provide a
 * permanent discount on the amount of food a player is required to pay during
 * the Sustenance phase of the game. Like all characters, their specific class type
 * ("GATHERER") also contributes to forming complete character sets for end-game scoring.
 * </p>
 */
public class Gatherer extends CharacterCard{
    private final int foodDiscount;

    /**
     * Constructs a new Gatherer character card.
     *
     * @param minPlayers   the minimum number of players required to include this card in the match
     * @param era          the {@link Era} to which this card belongs
     * @param id           the unique identifier for this card
     * @param foodDiscount the permanent food discount granted during Sustenance
     * @param type         the string classification of the card (e.g., "GATHERER")
     */
    public Gatherer(int minPlayers, Era era, String id, int foodDiscount, String type) {
        super(minPlayers, era, id, type);
        this.foodDiscount = foodDiscount;
    }

    /**
     * Retrieves the food discount provided by this gatherer.
     * <p>
     * Used by the player's Tribe to calculate the total reduction in food cost
     * when a Sustenance event is resolved.
     * </p>
     *
     * @return the food discount value
     */
    @Override
    public int getFoodDiscount() {
        return foodDiscount;
    }

    /**
     * Retrieves the specific string classification of this character.
     * <p>
     * Returning exactly "GATHERER" is crucial for the game engine to correctly track
     * distinct character classes when computing bonuses for complete character sets.
     * </p>
     *
     * @return the string literal representing the character type
     */
    @Override
    public String getType(){
        return type;
    }
}
