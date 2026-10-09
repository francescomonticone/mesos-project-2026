package it.polimi.ingsw.Server.Model.Cards.BuildingCards;

import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.BuildingEffect;
import it.polimi.ingsw.Server.Model.Cards.Card;
import it.polimi.ingsw.Server.Model.Match.Era;
import it.polimi.ingsw.Server.Model.Match.Player;

/**
 * Represents a Building card in the game.
 * <p>
 * This class extends the base {@link Card} class and introduces attributes
 * specific to buildings, such as the food cost required to build them,
 * the prestige points they grant at the end of the match, and their specific
 * {@link BuildingEffect}.
 * </p>
 */
public class BuildingCard extends Card {
    private final int foodCost;
    private final int finalPrestigePoints;
    private final BuildingEffect effect;

    /**
     * Constructs a new {@code BuildingCard} with the specified attributes.
     *
     * @param minPlayers          the minimum number of players required in the match to use this card
     * @param era                 the {@link Era} this card belongs to
     * @param id                  the unique identifier of the card (e.g., "BL_01")
     * @param foodCost            the amount of food required to pick or build this card
     * @param finalPrestigePoints the base prestige points this building grants at the end of the game
     * @param effect              the {@link BuildingEffect} representing the special ability of this building
     */
    public BuildingCard(int minPlayers, Era era, String id, int foodCost, int finalPrestigePoints, BuildingEffect effect) {
        super(id, era, minPlayers);
        this.foodCost = foodCost;
        this.finalPrestigePoints = finalPrestigePoints;
        this.effect = effect;
    }

    /**
     * Retrieves the food cost required to acquire this building.
     *
     * @return the food cost as an integer
     */
    @Override
    public int getFoodCost() {
        return foodCost;
    }

    /**
     * Retrieves the prestige points awarded by this building at the end of the match.
     *
     * @return the final prestige points as an integer
     */
    public int getFinalPrestigePoints() {
        return finalPrestigePoints;
    }

    /**
     * Retrieves the special effect associated with this building.
     *
     * @return the {@link BuildingEffect} of this card
     */
    public BuildingEffect getEffect() {
        return effect;
    }

    /**
     * Indicates whether this card can be directly picked from the board by a player.
     *
     * @return {@code true} since Building cards are always pickable
     */
    @Override
    public boolean isPickable() {
        return true;
    }

    /**
     * Adds this building card to the specified player's tribe.
     * <p>
     * This method uses the Double Dispatch pattern to ensure the card is stored
     * in the correct specific collection (buildings) within the player's tribe.
     * </p>
     *
     * @param player the {@link Player} who acquired the card
     */
    @Override
    public void addToTribe(Player player) {
       player.addBuildingToTribe(this); //this is a building so it will call the right method
    }
}
